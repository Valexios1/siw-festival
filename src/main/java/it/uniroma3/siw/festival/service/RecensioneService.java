package it.uniroma3.siw.festival.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.festival.exception.RecensioneNotFoundException;
import it.uniroma3.siw.festival.model.Recensione;
import it.uniroma3.siw.festival.repository.RecensioneRepository;

@Service
public class RecensioneService {

    @Autowired
    private RecensioneRepository recensioneRepository;

    @Transactional(readOnly = true)
    public List<Recensione> ultime5() {
        return recensioneRepository.findTop5ByOrderByDataCreazioneDesc();
    }

    @Transactional(readOnly = true)
    public List<Recensione> findAll() {
        return recensioneRepository.findAllByOrderByDataCreazioneDesc();
    }

    @Transactional(readOnly = true)
    public List<Recensione> findByFilm(Long filmId) {
        return recensioneRepository.findByFilmIdOrderByDataCreazioneDesc(filmId);
    }

    @Transactional(readOnly = true)
    public List<Recensione> findByUtente(Long utenteId) {
        return recensioneRepository.findByUtenteIdOrderByDataCreazioneDesc(utenteId);
    }

    @Transactional(readOnly = true)
    public Recensione findById(Long id) {
        return recensioneRepository.findById(id).orElseThrow(() -> new RecensioneNotFoundException(id));
    }

    /**
     * La recensione dell'utente per il film indicato, se esiste.
     */
    @Transactional(readOnly = true)
    public Recensione findByFilmAndUtente(Long filmId, Long utenteId) {
        return recensioneRepository.findByFilmIdAndUtenteId(filmId, utenteId).orElse(null);
    }

    /**
     * Un utente puo' recensire un film una sola volta: finche' non elimina la
     * recensione esistente non puo' scriverne un'altra per lo stesso film.
     */
    @Transactional(readOnly = true)
    public boolean puoRecensire(Long filmId, Long utenteId) {
        return filmId != null && utenteId != null
                && !recensioneRepository.existsByFilmIdAndUtenteId(filmId, utenteId);
    }

    @Transactional(readOnly = true)
    public Double mediaVoto(Long filmId) {
        return recensioneRepository.mediaVotoByFilmId(filmId);
    }

    @Transactional(readOnly = true)
    public long conteggio(Long filmId) {
        return recensioneRepository.countByFilmId(filmId);
    }

    @Transactional
    public Recensione salva(Recensione recensione) {
        if (recensioneRepository.existsByFilmIdAndUtenteId(recensione.getFilm().getId(), recensione.getUtente().getId())) {
            throw new IllegalArgumentException("Hai gia' scritto una recensione per questo film.");
        }
        recensione.setDataCreazione(LocalDateTime.now());
        return recensioneRepository.save(recensione);
    }

    /**
     * Aggiorna una recensione esistente: voto, testo e data di ultima modifica.
     * Autore e film non sono modificabili.
     */
    @Transactional
    public Recensione modifica(Long id, Recensione modifiche) {
        Recensione esistente = findById(id);
        esistente.setVoto(modifiche.getVoto());
        esistente.setTesto(modifiche.getTesto());
        esistente.setDataCreazione(LocalDateTime.now());
        return recensioneRepository.save(esistente);
    }

    @Transactional
    public void elimina(Long id) {
        Recensione recensione = findById(id);
        recensioneRepository.delete(recensione);
    }
}
