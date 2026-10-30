package it.uniroma3.siw.festival.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.festival.model.Festival;

public interface FestivalRepository extends CrudRepository<Festival, Long> {

    /*
     * Elenco dei festival, dal piu' recente al piu' vecchio: e' l'ordine con cui
     * vengono mostrati nella pagina pubblica.
     */
    List<Festival> findAllByOrderByAnnoDesc();

    /*
     * Festival con i film partecipanti caricati in una sola query (JOIN FETCH).
     * Serve al dettaglio festival: senza il fetch, iterare i film in pagina
     * genererebbe una query per ogni festival/collezione (problema N+1).
     */
    @Query("select distinct f from Festival f left join fetch f.film where f.id = :id")
    Optional<Festival> findByIdConFilm(@Param("id") Long id);
}
