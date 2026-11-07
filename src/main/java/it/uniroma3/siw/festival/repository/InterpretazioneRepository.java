package it.uniroma3.siw.festival.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.festival.model.Interpretazione;

public interface InterpretazioneRepository extends JpaRepository<Interpretazione, Long> {

    /*
     * Cast del film con gli attori caricati in un'unica query (JOIN FETCH).
     * Il dettaglio film mostra accanto a ogni personaggio il nome dell'attore:
     * senza il fetch servirebbe una query per ogni attore.
     */
    @Query("select i from Interpretazione i join fetch i.attore where i.film.id = :filmId "
            + "order by i.attore.cognome asc, i.attore.nome asc")
    List<Interpretazione> findByFilmIdOrderByAttoreCognomeAscAttoreNomeAsc(@Param("filmId") Long filmId);

    boolean existsByFilmIdAndAttoreId(Long filmId, Long attoreId);

    /*
     * Filmografia dell'attore con i film caricati in un'unica query (JOIN FETCH).
     * Il dettaglio attore mostra il film e il personaggio interpretato.
     */
    @Query("select i from Interpretazione i join fetch i.film where i.attore.id = :attoreId "
            + "order by i.film.titolo asc")
    List<Interpretazione> findByAttoreIdOrderByFilmTitoloAsc(@Param("attoreId") Long attoreId);
}
