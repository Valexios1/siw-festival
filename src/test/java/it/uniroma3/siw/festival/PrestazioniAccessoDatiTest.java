package it.uniroma3.siw.festival;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import it.uniroma3.siw.festival.model.Festival;
import it.uniroma3.siw.festival.model.Proiezione;
import it.uniroma3.siw.festival.repository.FestivalRepository;
import it.uniroma3.siw.festival.service.ProiezioneService;
import jakarta.persistence.EntityManagerFactory;

/**
 * Analisi sperimentale dell'accesso ai dati (sezione 8.2 del progetto).
 *
 * <p>Lo stesso caso d'uso - il programma di un festival con i film e le sale - viene
 * eseguito con tre strategie di fetch diverse, misurando per ciascuna il numero di
 * query SQL, il tempo di esecuzione e il numero di entita' caricate.
 *
 * <p>Le statistiche di Hibernate sono abilitate solo per questo test
 * ({@code hibernate.generate_statistics=true}).
 *
 * <p>Esecuzione: {@code mvnw test -Dtest=PrestazioniAccessoDatiTest}
 * (ricrea il database, perche' il contesto viene avviato con {@code ddl-auto=create}).
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate.SQL=warn",
        "logging.level.org.hibernate.type.descriptor.sql=warn"
})
class PrestazioniAccessoDatiTest {

    @Autowired
    private ProiezioneService proiezioneService;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void confrontoStrategieDiAccessoAlProgrammaDelFestival() {
        Festival festival = scegliFestivalConPiuProiezioni();
        Long festivalId = festival.getId();

        System.out.println();
        System.out.println("=== Test accesso al programma del festival ===");
        System.out.println("Festival: " + festival.getNome() + " (id=" + festivalId + ")");

        // Strategia 1: accesso tramite associazioni LAZY.
        // Il programma viene letto con una query; film e sala restano proxy e
        // vengono inizializzati uno per uno al primo accesso (N+1 query).
        misura("Strategia 1: LAZY", () -> {
            List<Proiezione> proiezioni = proiezioneService.findByFestivalSenzaFetch(festivalId);
            int righe = 0;
            for (Proiezione p : proiezioni) {
                // Ogni accesso a film, sala e festival puo' generare una query aggiuntiva.
                String descrizione = p.getFilm().getTitolo() + " / " + p.getSala().getNome()
                        + " / " + p.getFestival().getNome();
                righe += descrizione.isEmpty() ? 0 : 1;
            }
            return righe;
        });

        // Strategia 2: query con JOIN FETCH.
        // Film e sala arrivano con la stessa query del programma.
        misura("Strategia 2: JOIN FETCH", () -> {
            List<Proiezione> proiezioni = proiezioneService.findByFestivalConJoinFetch(festivalId);
            int righe = 0;
            for (Proiezione p : proiezioni) {
                String descrizione = p.getFilm().getTitolo() + " / " + p.getSala().getNome();
                righe += descrizione.isEmpty() ? 0 : 1;
            }
            return righe;
        });

        // Strategia 3: @EntityGraph.
        // Stessa ottimizzazione, dichiarata sull'interfaccia del repository.
        misura("Strategia 3: @EntityGraph", () -> {
            List<Proiezione> proiezioni = proiezioneService.findByFestivalConEntityGraph(festivalId);
            int righe = 0;
            for (Proiezione p : proiezioni) {
                String descrizione = p.getFilm().getTitolo() + " / " + p.getSala().getNome();
                righe += descrizione.isEmpty() ? 0 : 1;
            }
            return righe;
        });

        System.out.println();
    }

    /**
     * Esegue una strategia in una transazione dedicata (quindi con un persistence
     * context nuovo) e stampa le statistiche raccolte da Hibernate.
     */
    private void misura(String nomeStrategia, java.util.function.Supplier<Integer> strategia) {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        final int[] righe = new int[1];
        long inizio = System.nanoTime();
        // Ogni esecuzione ha la propria transazione: la prima-level cache non
        // interferisce tra una strategia e l'altra.
        transactionTemplate.execute(status -> righe[0] = strategia.get());
        long durataMs = (System.nanoTime() - inizio) / 1_000_000;

        System.out.println();
        System.out.println(nomeStrategia);
        System.out.println("Proiezioni caricate: " + righe[0]);
        System.out.println("Query SQL: " + statistics.getPrepareStatementCount());
        System.out.println("Entita' caricate: " + statistics.getEntityLoadCount());
        System.out.println("Tempo: " + durataMs + " ms");
    }

    /**
     * Sceglie il festival con piu' proiezioni, cosi' il confronto avviene sullo
     * stesso insieme di dati ed e' significativo.
     */
    private Festival scegliFestivalConPiuProiezioni() {
        Festival scelto = null;
        int massimo = -1;
        for (Festival f : festivalRepository.findAllByOrderByAnnoDesc()) {
            int quante = proiezioneService.findByFestivalSenzaFetch(f.getId()).size();
            if (quante > massimo) {
                massimo = quante;
                scelto = f;
            }
        }
        if (scelto == null) {
            throw new IllegalStateException("Nessun festival presente: eseguire import.sql prima del test.");
        }
        return scelto;
    }
}
