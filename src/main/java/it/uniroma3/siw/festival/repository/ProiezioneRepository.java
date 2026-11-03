package it.uniroma3.siw.festival.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.festival.model.Proiezione;

public interface ProiezioneRepository extends JpaRepository<Proiezione, Long> {

    /*
     * Programma completo. Film, sala e festival sono caricati con un EntityGraph:
     * ogni riga del programma li mostra, quindi senza il fetch servirebbe una
     * query per ogni proiezione (N+1).
     */
    @EntityGraph(attributePaths = { "film", "sala", "festival" })
    List<Proiezione> findAllByOrderByDataOraAsc();

    List<Proiezione> findByFestivalIdOrderByDataOraAsc(Long festivalId);

    @EntityGraph(attributePaths = { "sala", "festival" })
    List<Proiezione> findByFilmIdOrderByDataOraAsc(Long filmId);

    /*
     * Proiezioni della sala comprese nell'intervallo indicato.
     * E' la query usata dal controllo di disponibilita': l'intervallo di ricerca
     * e' volutamente piu' ampio dell'intervallo da verificare (vedi ProiezioneService),
     * cosi' il confronto preciso tra gli orari resta in Java.
     */
    List<Proiezione> findBySalaIdAndDataOraBetweenOrderByDataOraAsc(Long salaId, LocalDateTime inizio, LocalDateTime fine);

    boolean existsBySalaId(Long salaId);

    boolean existsByFilmId(Long filmId);

    boolean existsByFestivalId(Long festivalId);

    // ------------------------------------------------------------------
    // Varianti usate dall'analisi sperimentale dell'accesso ai dati:
    // stesso caso d'uso (programma di un festival con film e sale) risolto
    // con tre strategie di fetch diverse.
    // ------------------------------------------------------------------

    /*
     * Strategia 1 (LAZY): il programma viene caricato da solo; film e sala restano
     * proxy e vengono inizializzati uno per uno al primo accesso.
     */
    List<Proiezione> findByFestivalId(Long festivalId);

    /*
     * Strategia 2 (JOIN FETCH): film e sala vengono caricati con la stessa query
     * del programma, con due join (leciti perche' entrambe le associazioni sono
     * molti-a-uno, non collezioni).
     */
    @Query("select p from Proiezione p join fetch p.film join fetch p.sala "
            + "where p.festival.id = :festivalId order by p.dataOra asc")
    List<Proiezione> findByFestivalIdConFilmESala(@Param("festivalId") Long festivalId);

    /*
     * Strategia 3 (@EntityGraph): come la precedente, ma la strategia di fetch e'
     * dichiarata fuori dalla query, sull'annotazione @EntityGraph.
     * La query e' esplicita perche' il nome del metodo contiene "ConEntityGraph",
     * che non e' un nome di proprieta' e non sarebbe interpretabile come derived query.
     */
    @EntityGraph(attributePaths = { "film", "sala" })
    @Query("select p from Proiezione p where p.festival.id = :festivalId order by p.dataOra asc")
    List<Proiezione> findByFestivalIdConEntityGraph(@Param("festivalId") Long festivalId);
}
