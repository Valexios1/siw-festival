package it.uniroma3.siw.festival.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.festival.model.Recensione;

public interface RecensioneRepository extends JpaRepository<Recensione, Long> {

    /*
     * Recensioni di un film, dalla piu' recente: popolano il dettaglio film,
     * che mostra l'autore di ogni recensione (da qui l'EntityGraph su utente).
     */
    @EntityGraph(attributePaths = { "utente" })
    List<Recensione> findByFilmIdOrderByDataCreazioneDesc(Long filmId);

    /*
     * Le recensioni scritte da un utente, dalla piu' recente: popolano l'area personale,
     * che mostra il titolo del film recensito (da qui l'EntityGraph su film).
     */
    @EntityGraph(attributePaths = { "film" })
    List<Recensione> findByUtenteIdOrderByDataCreazioneDesc(Long utenteId);

    /*
     * La recensione dell'utente indicato per il film indicato: un utente puo'
     * scrivere al massimo una recensione per film.
     */
    Optional<Recensione> findByFilmIdAndUtenteId(Long filmId, Long utenteId);

    boolean existsByFilmIdAndUtenteId(Long filmId, Long utenteId);

    /*
     * Le 5 recensioni piu' recenti, usate nella pagina iniziale
     * (mostra autore e film recensito).
     */
    @EntityGraph(attributePaths = { "utente", "film" })
    List<Recensione> findTop5ByOrderByDataCreazioneDesc();

    /*
     * Tutte le recensioni, dalla piu' recente: usata dalla pagina di moderazione.
     */
    @EntityGraph(attributePaths = { "utente", "film" })
    List<Recensione> findAllByOrderByDataCreazioneDesc();
}
