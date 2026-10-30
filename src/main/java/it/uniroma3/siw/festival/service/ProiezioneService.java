package it.uniroma3.siw.festival.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.festival.exception.FestivalNotFoundException;
import it.uniroma3.siw.festival.exception.FilmNotFoundException;
import it.uniroma3.siw.festival.exception.ProiezioneNotFoundException;
import it.uniroma3.siw.festival.exception.SalaNotFoundException;
import it.uniroma3.siw.festival.model.Festival;
import it.uniroma3.siw.festival.model.Film;
import it.uniroma3.siw.festival.model.Proiezione;
import it.uniroma3.siw.festival.model.Sala;
import it.uniroma3.siw.festival.repository.FestivalRepository;
import it.uniroma3.siw.festival.repository.FilmRepository;
import it.uniroma3.siw.festival.repository.ProiezioneRepository;
import it.uniroma3.siw.festival.repository.SalaRepository;

@Service
public class ProiezioneService {

    private static final DateTimeFormatter FORMATO_DATA_ORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private ProiezioneRepository proiezioneRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private SalaRepository salaRepository;

    @Transactional(readOnly = true)
    public List<Proiezione> findAll() {
        return proiezioneRepository.findAllByOrderByDataOraAsc();
    }

    @Transactional(readOnly = true)
    public Proiezione findById(Long id) {
        return proiezioneRepository.findById(id).orElseThrow(() -> new ProiezioneNotFoundException(id));
    }

    /*
     * Programma di un festival con film e sala gia' caricati (JOIN FETCH):
     * e' la strategia usata dal dettaglio festival, dove ogni riga mostra
     * film e sala.
     */
    @Transactional(readOnly = true)
    public List<Proiezione> findByFestival(Long festivalId) {
        return proiezioneRepository.findByFestivalIdConFilmESala(festivalId);
    }

    @Transactional(readOnly = true)
    public List<Proiezione> findByFilm(Long filmId) {
        return proiezioneRepository.findByFilmIdOrderByDataOraAsc(filmId);
    }

    /*
     * Variante senza fetch: film e sala restano associazioni LAZY.
     * E' la strategia di riferimento per l'analisi sperimentale dell'accesso
     * ai dati (PrestazioniAccessoDatiTest).
     */
    @Transactional(readOnly = true)
    public List<Proiezione> findByFestivalSenzaFetch(Long festivalId) {
        return proiezioneRepository.findByFestivalIdOrderByDataOraAsc(festivalId);
    }

    @Transactional(readOnly = true)
    public List<Proiezione> findByFestivalConEntityGraph(Long festivalId) {
        return proiezioneRepository.findByFestivalIdConEntityGraph(festivalId);
    }

    @Transactional(readOnly = true)
    public List<Proiezione> findByFestivalConJoinFetch(Long festivalId) {
        return proiezioneRepository.findByFestivalIdConFilmESala(festivalId);
    }

    /**
     * Programma una nuova proiezione.
     *
     * <p>E' il caso d'uso che coinvolge piu' entita' e piu' repository: nella stessa
     * transazione vengono recuperati il festival, il film e la sala, si verifica che
     * il film partecipi al festival e che la sala non sia gia' occupata nello stesso
     * intervallo temporale, e solo alla fine la proiezione viene salvata.
     *
     * <p>L'operazione e' atomica: se una qualsiasi verifica fallisce viene sollevata
     * un'eccezione, la transazione viene marcata per il rollback e nessuna riga
     * rimane scritta.
     *
     * <p>Livello di isolamento: con il default READ_COMMITTED due transazioni
     * concorrenti potrebbero superare entrambe la verifica di disponibilita' e
     * inserire due proiezioni sovrapposte (write skew). Il controllo applicativo
     * copre quindi il caso d'uso normale; per escludere anche le esecuzioni
     * concorrenti servirebbe un vincolo a livello di database (un EXCLUDE constraint
     * sull'intervallo occupato) oppure l'isolamento SERIALIZABLE, che farebbe
     * fallire una delle due transazioni al commit.
     */
    @Transactional
    public Proiezione programmaProiezione(Proiezione proiezione) {
        risolviRiferimenti(proiezione);
        valida(proiezione, null);
        return proiezioneRepository.save(proiezione);
    }

    /**
     * Modifica una proiezione esistente: la verifica di disponibilita' esclude la
     * proiezione stessa, che altrimenti risulterebbe in conflitto con se stessa.
     */
    @Transactional
    public Proiezione modificaProiezione(Long id, Proiezione modifiche) {
        Proiezione esistente = findById(id);
        risolviRiferimenti(modifiche);

        esistente.setFestival(modifiche.getFestival());
        esistente.setFilm(modifiche.getFilm());
        esistente.setSala(modifiche.getSala());
        esistente.setDataOra(modifiche.getDataOra());
        if (modifiche.getStato() != null) {
            esistente.setStato(modifiche.getStato());
        }

        valida(esistente, id);
        return proiezioneRepository.save(esistente);
    }

    /**
     * Aggiorna lo stato della proiezione (PROGRAMMATA, COMPLETATA, ANNULLATA).
     */
    @Transactional
    public Proiezione cambiaStato(Long id, Proiezione.Stato stato) {
        Proiezione proiezione = findById(id);
        proiezione.setStato(stato);
        return proiezioneRepository.save(proiezione);
    }

    @Transactional
    public void eliminaProiezione(Long id) {
        Proiezione proiezione = findById(id);
        proiezioneRepository.delete(proiezione);
    }

    /**
     * Sostituisce i riferimenti arrivati dal form (entita' "stub" con il solo id)
     * con le entita' gestite dal persistence context, verificandone l'esistenza.
     */
    private void risolviRiferimenti(Proiezione proiezione) {
        Long festivalId = proiezione.getFestival() != null ? proiezione.getFestival().getId() : null;
        Long filmId = proiezione.getFilm() != null ? proiezione.getFilm().getId() : null;
        Long salaId = proiezione.getSala() != null ? proiezione.getSala().getId() : null;

        if (festivalId == null || filmId == null || salaId == null) {
            throw new IllegalArgumentException("festival, film e sala della proiezione sono obbligatori");
        }

        Festival festival = festivalRepository.findById(festivalId).orElseThrow(() -> new FestivalNotFoundException(festivalId));
        Film film = filmRepository.findById(filmId).orElseThrow(() -> new FilmNotFoundException(filmId));
        Sala sala = salaRepository.findById(salaId).orElseThrow(() -> new SalaNotFoundException(salaId));

        proiezione.setFestival(festival);
        proiezione.setFilm(film);
        proiezione.setSala(sala);

        if (proiezione.getStato() == null) {
            proiezione.setStato(Proiezione.Stato.PROGRAMMATA);
        }
    }

    /**
     * Verifica la consistenza della proiezione: il film deve partecipare al festival
     * e la sala non deve essere occupata da un'altra proiezione nello stesso
     * intervallo temporale [inizio, inizio + durata del film).
     *
     * @param idDaEscludere proiezione da ignorare nel controllo (fase di modifica)
     */
    private void valida(Proiezione proiezione, Long idDaEscludere) {
        Festival festival = proiezione.getFestival();
        Film film = proiezione.getFilm();
        Sala sala = proiezione.getSala();
        LocalDateTime inizio = proiezione.getDataOra();

        if (inizio == null) {
            throw new IllegalArgumentException("la data e l'ora della proiezione sono obbligatorie");
        }

        // Un film puo' essere proiettato in un festival solo se vi partecipa.
        boolean partecipa = film.getFestival() != null && film.getFestival().stream()
                .anyMatch(f -> f.getId().equals(festival.getId()));
        if (!partecipa) {
            throw new IllegalArgumentException("il film " + film.getTitolo() + " non partecipa al festival "
                    + festival.getNome() + ": associalo al festival prima di programmarne la proiezione.");
        }

        LocalDateTime fine = inizio.plusMinutes(film.getDurata());

        /*
         * Si cercano le proiezioni della sala in una finestra piu' ampia di quella
         * da verificare (un giorno prima e un giorno dopo): la finestra serve solo a
         * ridurre i dati letti, mentre il confronto esatto tra gli intervalli resta
         * in Java, dove si conosce la durata di ogni film.
         */
        List<Proiezione> candidate = proiezioneRepository.findBySalaIdAndDataOraBetweenOrderByDataOraAsc(
                sala.getId(), inizio.minusDays(1), inizio.plusDays(1));

        for (Proiezione esistente : candidate) {
            if (idDaEscludere != null && idDaEscludere.equals(esistente.getId())) {
                continue; // e' la proiezione che si sta modificando
            }
            if (esistente.getStato() == Proiezione.Stato.ANNULLATA) {
                continue; // una proiezione annullata non occupa la sala
            }

            // Gli intervalli si sovrappongono se uno inizia prima che l'altro finisca.
            if (inizio.isBefore(esistente.getDataOraFine()) && fine.isAfter(esistente.getDataOra())) {
                throw new IllegalArgumentException("la sala " + sala.getNome() + " e' gia' occupata dalla proiezione di "
                        + esistente.getFilm().getTitolo() + " (" + FORMATO_DATA_ORA.format(esistente.getDataOra())
                        + " - " + FORMATO_DATA_ORA.format(esistente.getDataOraFine())
                        + "), che si sovrappone all'orario richiesto (" + FORMATO_DATA_ORA.format(inizio)
                        + " - " + FORMATO_DATA_ORA.format(fine) + ").");
            }
        }
    }
}
