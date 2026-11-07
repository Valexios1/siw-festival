package it.uniroma3.siw.festival;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Locale;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import it.uniroma3.siw.festival.model.Film;

/**
 * Analisi sperimentale dell'accesso ai dati (sezione 8.2).
 *
 * <p>Caso d'uso: caricamento di tutti i film di un festival con le relative
 * proiezioni. Le stesse righe vengono lette tre volte, una per strategia:
 *
 * <ol>
 *   <li>LAZY: si caricano i film e poi si accede alla collezione per ogni film
 *       (comportamento N+1);</li>
 *   <li>JOIN FETCH: film e proiezioni arrivano con una sola query JPQL;</li>
 *   <li>ENTITY GRAPH: come il join fetch, ma il grafo di fetch e' dichiarato
 *       tramite {@link EntityGraph} invece che in JPQL.</li>
 * </ol>
 *
 * <p>Per ogni strategia vengono stampati il numero di query SQL eseguite
 * (Hibernate {@link Statistics#getPrepareStatementCount()}), il tempo
 * complessivo e il numero di oggetti caricati.
 *
 * <p>Esecuzione: {@code mvnw.cmd -Dtest=AnalisiAccessoDatiTest test}
 * (su Linux/macOS {@code ./mvnw -Dtest=AnalisiAccessoDatiTest test}).
 */
@SpringBootTest(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.properties.hibernate.session.events.log=false",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate.SQL=warn",
        "logging.level.org.hibernate.type.descriptor.sql=warn",
        "logging.level.org.hibernate.orm.jdbc.bind=warn"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnalisiAccessoDatiTest {

    private static final String NOME_FESTIVAL = "Festival del Cinema di Roma";

    private static final String QUERY_FILM_DEL_FESTIVAL =
            "select f from Film f join f.festival fe where fe.id = :festivalId order by f.titolo";

    private static final String QUERY_FILM_CON_PROIEZIONI =
            "select distinct f from Film f join f.festival fe left join fetch f.proiezioni "
                    + "where fe.id = :festivalId order by f.titolo";

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;
    private Statistics statistics;
    private Long festivalId;

    @BeforeAll
    void prepara() {
        this.transactionTemplate = new TransactionTemplate(this.transactionManager);
        this.statistics = this.entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        this.festivalId = this.transactionTemplate.execute(status -> this.em.createQuery(
                        "select f.id from Festival f where f.nome = :nome", Long.class)
                .setParameter("nome", NOME_FESTIVAL)
                .getSingleResult());
    }

    @Test
    void confrontaStrategieDiAccessoAiFilmDelFestival() {
        // Warm-up: la prima esecuzione di ogni query compila il piano e scalda la cache.
        esegui("warm-up LAZY", this::caricaConLazy);
        esegui("warm-up JOIN FETCH", this::caricaConJoinFetch);
        esegui("warm-up ENTITY GRAPH", this::caricaConEntityGraph);

        Risultato lazy = esegui("LAZY", this::caricaConLazy);
        Risultato joinFetch = esegui("JOIN FETCH", this::caricaConJoinFetch);
        Risultato entityGraph = esegui("ENTITY GRAPH", this::caricaConEntityGraph);

        // Le tre strategie devono restituire lo stesso insieme di dati.
        assertEquals(lazy.caricamento().filmIds(), joinFetch.caricamento().filmIds());
        assertEquals(lazy.caricamento().filmIds(), entityGraph.caricamento().filmIds());
        assertEquals(lazy.caricamento().proiezioniCaricate(), joinFetch.caricamento().proiezioniCaricate());
        assertEquals(lazy.caricamento().proiezioniCaricate(), entityGraph.caricamento().proiezioniCaricate());
        assertFalse(lazy.caricamento().filmIds().isEmpty());

        stampa(lazy);
        stampa(joinFetch);
        stampa(entityGraph);
    }

    /**
     * Strategia LAZY: la query carica solo i film; la collezione {@code proiezioni}
     * viene inizializzata quando il codice la legge, con una query per ogni film.
     */
    private Caricamento caricaConLazy() {
        List<Film> film = this.em.createQuery(QUERY_FILM_DEL_FESTIVAL, Film.class)
                .setParameter("festivalId", this.festivalId)
                .getResultList();

        int proiezioni = 0;
        for (Film f : film) {
            proiezioni += f.getProiezioni().size();
        }
        return new Caricamento(ids(film), film.size(), proiezioni);
    }

    /**
     * Strategia JOIN FETCH: una sola query porta film e proiezioni, quindi
     * {@code getProiezioni()} non genera query aggiuntive.
     */
    private Caricamento caricaConJoinFetch() {
        List<Film> film = this.em.createQuery(QUERY_FILM_CON_PROIEZIONI, Film.class)
                .setParameter("festivalId", this.festivalId)
                .getResultList();

        int proiezioni = 0;
        for (Film f : film) {
            proiezioni += f.getProiezioni().size();
        }
        return new Caricamento(ids(film), film.size(), proiezioni);
    }

    /**
     * Strategia ENTITY GRAPH: la query e' la stessa della strategia LAZY, ma il
     * grafo di fetch chiede a Hibernate di caricare {@code proiezioni} insieme al film.
     */
    private Caricamento caricaConEntityGraph() {
        EntityGraph<Film> grafo = this.em.createEntityGraph(Film.class);
        grafo.addAttributeNodes("proiezioni");

        List<Film> film = this.em.createQuery(QUERY_FILM_DEL_FESTIVAL, Film.class)
                .setParameter("festivalId", this.festivalId)
                .setHint("jakarta.persistence.fetchgraph", grafo)
                .getResultList();

        int proiezioni = 0;
        for (Film f : film) {
            proiezioni += f.getProiezioni().size();
        }
        return new Caricamento(ids(film), film.size(), proiezioni);
    }

    /**
     * Esegue una strategia in una transazione dedicata (quindi con un persistence
     * context nuovo) e misura query SQL e tempo. Le entita' restano attaccate al
     * contesto solo per la durata della transazione: ogni strategia riparte da zero.
     */
    private Risultato esegui(String nome, Strategia strategia) {
        this.statistics.clear();
        long inizio = System.nanoTime();
        Caricamento caricamento = this.transactionTemplate.execute(status -> strategia.applica());
        long durataNanos = System.nanoTime() - inizio;
        return new Risultato(nome, caricamento, this.statistics.getPrepareStatementCount(), durataNanos);
    }

    private static List<Long> ids(List<Film> film) {
        return film.stream().map(Film::getId).toList();
    }

    private void stampa(Risultato risultato) {
        System.out.println();
        System.out.printf("Strategia: %s%n", risultato.nome());
        System.out.printf("Film caricati: %d%n", risultato.caricamento().filmCaricati());
        System.out.printf("Proiezioni caricate: %d%n", risultato.caricamento().proiezioniCaricate());
        System.out.printf("Query SQL: %d%n", risultato.querySql());
        System.out.printf(Locale.ROOT, "Tempo: %.2f ms%n", risultato.durataNanos() / 1_000_000.0);
    }

    @FunctionalInterface
    private interface Strategia {
        Caricamento applica();
    }

    private record Caricamento(List<Long> filmIds, int filmCaricati, int proiezioniCaricate) {
    }

    private record Risultato(String nome, Caricamento caricamento, long querySql, long durataNanos) {
    }
}
