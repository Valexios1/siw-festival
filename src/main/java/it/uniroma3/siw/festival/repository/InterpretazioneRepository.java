package it.uniroma3.siw.festival.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.festival.model.Interpretazione;

public interface InterpretazioneRepository extends CrudRepository<Interpretazione, Long> {

    List<Interpretazione> findByFilmId(Long filmId);

    /*
     * Cast del film con gli attori caricati in un'unica query: e' la variante
     * usata dal dettaglio film, che mostra accanto a ogni personaggio il nome
     * dell'attore (senza fetch servirebbe una query per attore).
     */
    @Query("select i from Interpretazione i join fetch i.attore where i.film.id = :filmId "
            + "order by i.attore.cognome asc, i.attore.nome asc")
    List<Interpretazione> findByFilmIdConAttore(@Param("filmId") Long filmId);

    boolean existsByFilmIdAndAttoreId(Long filmId, Long attoreId);
}
