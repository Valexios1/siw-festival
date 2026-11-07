package it.uniroma3.siw.festival.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.festival.exception.FestivalNotFoundException;
import it.uniroma3.siw.festival.exception.FilmNotFoundException;
import it.uniroma3.siw.festival.model.Festival;
import it.uniroma3.siw.festival.model.Film;
import it.uniroma3.siw.festival.repository.FestivalRepository;
import it.uniroma3.siw.festival.repository.FilmRepository;
import it.uniroma3.siw.festival.repository.ProiezioneRepository;

@Service
public class FestivalService {

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private ProiezioneRepository proiezioneRepository;

    /*
     * Operazione di sola lettura: la transazione viene aperta in readOnly, cosi'
     * Hibernate non esegue il dirty checking al termine e il database puo'
     * ottimizzare la lettura.
     */
    @Transactional(readOnly = true)
    public List<Festival> findAll() {
        return festivalRepository.findAllByOrderByAnnoDesc();
    }

    /*
     * Il festival viene caricato insieme ai film partecipanti (EntityGraph dichiarato
     * su FestivalRepository.findById): il dettaglio mostra i film e non serve quindi
     * una query per ognuno di essi.
     */
    @Transactional(readOnly = true)
    public Festival findById(Long id) {
        return festivalRepository.findById(id).orElseThrow(() -> new FestivalNotFoundException(id));
    }

    @Transactional
    public Festival save(Festival festival) {
        valida(festival);
        return festivalRepository.save(festival);
    }

    @Transactional
    public Festival modifica(Long id, Festival modifiche) {
        Festival esistente = findById(id);
        esistente.setNome(modifiche.getNome());
        esistente.setAnno(modifiche.getAnno());
        esistente.setCitta(modifiche.getCitta());
        esistente.setDataInizio(modifiche.getDataInizio());
        esistente.setDataFine(modifiche.getDataFine());
        esistente.setDescrizione(modifiche.getDescrizione());

        valida(esistente);
        return festivalRepository.save(esistente);
    }

    /**
     * Associa al festival esattamente i film indicati: la lista inviata dal form
     * sostituisce quella precedente, quindi l'operazione copre sia l'associazione
     * sia la rimozione di un film dal festival.
     *
     * <p>Quando un film viene rimosso dal festival, vengono cancellate prima tutte
     * le proiezioni di quel film in quel festival: una proiezione richiede che il
     * film partecipi al festival, quindi non puo' restare orfana.
     *
     * <p>La transazione comprende la verifica dei film richiesti, l'eliminazione
     * delle proiezioni dei film rimossi e l'aggiornamento della tabella di join:
     * se una delle operazioni fallisce, nessuna modifica viene applicata.
     */
    @Transactional
    public Festival associaFilm(Long festivalId, List<Long> filmIds) {
        Festival festival = findById(festivalId);

        Set<Long> nuoviId = filmIds == null ? Set.of()
                : filmIds.stream().filter(id -> id != null).collect(Collectors.toSet());
        Set<Long> attualiId = festival.getFilm() == null ? Set.of()
                : festival.getFilm().stream().map(Film::getId).collect(Collectors.toSet());

        // Se un film viene tolto dal festival, le sue proiezioni in quel festival
        // non sono piu' valide e vanno eliminate prima di aggiornare l'associazione.
        for (Long filmId : attualiId) {
            if (!nuoviId.contains(filmId)) {
                proiezioneRepository.deleteByFestivalIdAndFilmId(festivalId, filmId);
            }
        }

        if (!nuoviId.isEmpty()) {
            // JpaRepository.findAllById restituisce direttamente una List
            List<Film> film = filmRepository.findAllById(nuoviId);

            // Verifica che tutti gli id richiesti siano stati trovati
            if (film.size() != nuoviId.size()) {
                throw new FilmNotFoundException(0L);
            }
            festival.setFilm(film);
        } else {
            festival.getFilm().clear(); // Svuota la lista se filmIds e' nullo o vuoto
        }

        return festivalRepository.save(festival);
    }

    /**
     * Regola di consistenza: un festival non puo' finire prima di iniziare.
     */
    private void valida(Festival festival) {
        if (festival.getDataInizio() != null && festival.getDataFine() != null
                && festival.getDataFine().isBefore(festival.getDataInizio())) {
            throw new IllegalArgumentException("la data di fine (" + festival.getDataFine()
                    + ") non puo' precedere la data di inizio (" + festival.getDataInizio() + ")");
        }
    }
}
