package it.uniroma3.siw.festival.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.festival.exception.SalaNotFoundException;
import it.uniroma3.siw.festival.model.Sala;
import it.uniroma3.siw.festival.repository.SalaRepository;

@Service
public class SalaService {

    @Autowired
    private SalaRepository salaRepository;

    @Transactional(readOnly = true)
    public List<Sala> findAll() {
        return salaRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Sala findById(Long id) {
        return salaRepository.findById(id).orElseThrow(() -> new SalaNotFoundException(id));
    }

    @Transactional
    public Sala save(Sala sala) {
        if (salaRepository.existsByNome(sala.getNome())) {
            throw new IllegalArgumentException("Esiste gia' una sala con il nome " + sala.getNome() + ".");
        }
        return salaRepository.save(sala);
    }

    @Transactional
    public Sala modifica(Long id, Sala modifiche) {
        Sala esistente = findById(id);

        // Il nome e' l'identificatore naturale della sala: se cambia, non deve
        // entrare in conflitto con quello di un'altra sala.
        if (!esistente.getNome().equals(modifiche.getNome()) && salaRepository.existsByNome(modifiche.getNome())) {
            throw new IllegalArgumentException("Esiste gia' una sala con il nome " + modifiche.getNome() + ".");
        }

        esistente.setNome(modifiche.getNome());
        esistente.setIndirizzo(modifiche.getIndirizzo());
        esistente.setCapienza(modifiche.getCapienza());
        return salaRepository.save(esistente);
    }
}
