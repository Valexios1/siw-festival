package it.uniroma3.siw.festival.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.festival.exception.AttoreNotFoundException;
import it.uniroma3.siw.festival.exception.FilmNotFoundException;
import it.uniroma3.siw.festival.model.Attore;
import it.uniroma3.siw.festival.model.Film;
import it.uniroma3.siw.festival.model.Interpretazione;
import it.uniroma3.siw.festival.repository.AttoreRepository;
import it.uniroma3.siw.festival.repository.FilmRepository;
import it.uniroma3.siw.festival.repository.InterpretazioneRepository;

@Service
public class FilmService {

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private InterpretazioneRepository interpretazioneRepository;

    @Autowired
    private AttoreRepository attoreRepository;

    @Transactional(readOnly = true)
    public List<Film> findAll() {
        return filmRepository.findAllByOrderByTitoloAsc();
    }

    @Transactional(readOnly = true)
    public Film findById(Long id) {
        return filmRepository.findById(id).orElseThrow(() -> new FilmNotFoundException(id));
    }

    /*
     * Dettaglio film: i festival ai quali il film partecipa vengono caricati con la
     * stessa query del film. Il cast e' una seconda collezione e viene caricato da
     * una query separata (vedi getCast), perche' Hibernate non consente il fetch
     * di due "bag" nella stessa query.
     */
    @Transactional(readOnly = true)
    public Film findByIdConFestival(Long id) {
        return filmRepository.findByIdConFestival(id).orElseThrow(() -> new FilmNotFoundException(id));
    }

    /*
     * Cast del film con gli attori gia' caricati: evita una query per ogni
     * interpretazione mostrata nella pagina di dettaglio.
     */
    @Transactional(readOnly = true)
    public List<Interpretazione> getCast(Long filmId) {
        return interpretazioneRepository.findByFilmIdConAttore(filmId);
    }

    @Transactional
    public Film save(Film film) {
        return filmRepository.save(film);
    }

    @Transactional
    public Film modifica(Long id, Film modifiche) {
        Film esistente = findById(id);
        esistente.setTitolo(modifiche.getTitolo());
        esistente.setAnno(modifiche.getAnno());
        esistente.setDurata(modifiche.getDurata());
        esistente.setGenere(modifiche.getGenere());
        esistente.setPaeseProduzione(modifiche.getPaeseProduzione());
        esistente.setRegista(modifiche.getRegista());
        return filmRepository.save(esistente);
    }

    /**
     * Aggiunge un'attore al cast del film con il personaggio interpretato.
     *
     * <p>L'operazione coinvolge due entita' (film e attore) e l'entita' di join:
     * viene eseguita in un'unica transazione, verificando prima che l'attore non
     * sia gia' presente nel cast dello stesso film.
     */
    @Transactional
    public void aggiungiInterpretazione(Long filmId, Long attoreId, String personaggio) {
        Film film = findById(filmId);
        Attore attore = attoreRepository.findById(attoreId)
                .orElseThrow(() -> new AttoreNotFoundException(attoreId));

        if (personaggio == null || personaggio.isBlank()) {
            throw new IllegalArgumentException("il personaggio interpretato e' obbligatorio");
        }
        if (interpretazioneRepository.existsByFilmIdAndAttoreId(filmId, attoreId)) {
            throw new IllegalArgumentException(attore.getNome() + " " + attore.getCognome()
                    + " e' gia' presente nel cast di " + film.getTitolo() + ".");
        }

        Interpretazione interpretazione = new Interpretazione();
        interpretazione.setFilm(film);
        interpretazione.setAttore(attore);
        interpretazione.setPersonaggio(personaggio.trim());
        interpretazioneRepository.save(interpretazione);
    }

    /**
     * Rimuove una interpretazione dal cast del film, verificando che appartenga
     * davvero al film indicato.
     */
    @Transactional
    public void rimuoviInterpretazione(Long filmId, Long interpretazioneId) {
        Interpretazione interpretazione = interpretazioneRepository.findById(interpretazioneId)
                .orElseThrow(() -> new IllegalArgumentException("Interpretazione con id " + interpretazioneId + " non trovata."));

        if (!interpretazione.getFilm().getId().equals(filmId)) {
            throw new IllegalArgumentException("L'interpretazione non appartiene al film indicato.");
        }
        interpretazioneRepository.delete(interpretazione);
    }
}
