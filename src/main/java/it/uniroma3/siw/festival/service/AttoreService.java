package it.uniroma3.siw.festival.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.festival.exception.AttoreNotFoundException;
import it.uniroma3.siw.festival.model.Attore;
import it.uniroma3.siw.festival.model.Interpretazione;
import it.uniroma3.siw.festival.repository.AttoreRepository;
import it.uniroma3.siw.festival.repository.InterpretazioneRepository;

@Service
public class AttoreService {

    @Autowired
    private AttoreRepository attoreRepository;

    @Autowired
    private InterpretazioneRepository interpretazioneRepository;

    @Transactional(readOnly = true)
    public List<Attore> findAll() {
        return attoreRepository.findAllByOrderByCognomeAscNomeAsc();
    }

    @Transactional(readOnly = true)
    public Attore findById(Long id) {
        return attoreRepository.findById(id).orElseThrow(() -> new AttoreNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Interpretazione> getFilmografia(Long attoreId) {
        return interpretazioneRepository.findByAttoreIdOrderByFilmTitoloAsc(attoreId);
    }

    @Transactional
    public Attore save(Attore attore) {
        valida(attore);
        return attoreRepository.save(attore);
    }

    @Transactional
    public Attore modifica(Long id, Attore modifiche) {
        Attore esistente = findById(id);
        esistente.setNome(modifiche.getNome());
        esistente.setCognome(modifiche.getCognome());
        esistente.setDataNascita(modifiche.getDataNascita());
        esistente.setNazionalita(modifiche.getNazionalita());

        valida(esistente);
        return attoreRepository.save(esistente);
    }

    /**
     * Regola di consistenza: la data di nascita non puo' essere nel futuro.
     */
    private void valida(Attore attore) {
        if (attore.getDataNascita() != null && attore.getDataNascita().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("la data di nascita non puo' essere nel futuro");
        }
    }
}
