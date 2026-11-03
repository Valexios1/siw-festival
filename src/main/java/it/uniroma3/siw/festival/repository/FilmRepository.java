package it.uniroma3.siw.festival.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.festival.model.Film;

public interface FilmRepository extends JpaRepository<Film, Long> {

    List<Film> findAllByOrderByTitoloAsc();

    /*
     * Film con i festival ai quali partecipa caricati con un JOIN FETCH.
     * Il dettaglio film mostra i festival: con il fetch si evita una query
     * per ogni festival della lista.
     */
    @Query("select distinct f from Film f left join fetch f.festival where f.id = :id")
    Optional<Film> findByIdConFestival(@Param("id") Long id);

    /*
     * Film con il cast (interpretazioni e relativi attori) caricato con un JOIN FETCH.
     *
     * Il fetch della collezione "cast" e quello di "festival" non possono stare
     * nella stessa query: Hibernate rifiuta il fetch simultaneo di due "bag"
     * (MultipleBagFetchException). Le due collezioni vengono quindi caricate da
     * due query separate, eseguite nella stessa transazione.
     */
    @Query("select distinct f from Film f left join fetch f.cast i left join fetch i.attore where f.id = :id")
    Optional<Film> findByIdConCast(@Param("id") Long id);
}
