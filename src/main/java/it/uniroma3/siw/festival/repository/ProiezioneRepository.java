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

    @EntityGraph(attributePaths = { "sala", "festival" })
    List<Proiezione> findByFilmIdOrderByDataOraAsc(Long filmId);

    /*
     * Proiezioni della sala comprese nell'intervallo indicato.
     * E' la query usata dal controllo di disponibilita': l'intervallo di ricerca
     * e' volutamente piu' ampio dell'intervallo da verificare (vedi ProiezioneService),
     * cosi' il confronto preciso tra gli orari resta in Java.
     */
    List<Proiezione> findBySalaIdAndDataOraBetweenOrderByDataOraAsc(Long salaId, LocalDateTime inizio, LocalDateTime fine);

    /*
     * Programma di un festival con film e sala caricati con la stessa query
     * (JOIN FETCH): e' la query usata dal dettaglio festival, dove ogni riga mostra
     * il film e la sala della proiezione. Due join nella stessa query sono leciti
     * perche' entrambe le associazioni sono molti-a-uno, non collezioni.
     */
    @Query("select p from Proiezione p join fetch p.film join fetch p.sala "
            + "where p.festival.id = :festivalId order by p.dataOra asc")
    List<Proiezione> findByFestivalIdConFilmESala(@Param("festivalId") Long festivalId);

    boolean existsBySalaId(Long salaId);

    boolean existsByFilmId(Long filmId);

    boolean existsByFestivalId(Long festivalId);
}
