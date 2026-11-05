package it.uniroma3.siw.festival.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.festival.model.Film;

public interface FilmRepository extends JpaRepository<Film, Long> {

    List<Film> findAllByOrderByTitoloAsc();

    /*
     * Il film si carica sempre insieme ai festival ai quali partecipa: il dettaglio
     * mostra l'elenco dei festival, quindi senza fetch servirebbe una query per
     * ciascuno di essi.
     *
     * Il cast non entra in questo grafo: "festival" e "cast" sono due collezioni e
     * Hibernate non consente il fetch simultaneo di due "bag" nella stessa query
     * (MultipleBagFetchException). Il cast viene caricato a parte da
     * InterpretazioneRepository.findByFilmIdOrderByAttoreCognomeAscAttoreNomeAsc.
     */
    @Override
    @EntityGraph(attributePaths = { "festival" })
    Optional<Film> findById(Long id);

}
