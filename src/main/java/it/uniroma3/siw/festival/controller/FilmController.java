package it.uniroma3.siw.festival.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.festival.model.Credentials;
import it.uniroma3.siw.festival.model.Film;
import it.uniroma3.siw.festival.model.User;
import it.uniroma3.siw.festival.service.AttoreService;
import it.uniroma3.siw.festival.service.CredentialService;
import it.uniroma3.siw.festival.service.FilmService;
import it.uniroma3.siw.festival.service.ProiezioneService;
import it.uniroma3.siw.festival.service.RecensioneService;
import jakarta.validation.Valid;

@Controller
public class FilmController {

    @Autowired
    private FilmService filmService;

    @Autowired
    private AttoreService attoreService;

    @Autowired
    private ProiezioneService proiezioneService;

    @Autowired
    private RecensioneService recensioneService;

    @Autowired
    private CredentialService credentialService;

    // ------------------------------------------------------------------
    // Funzionalita' pubbliche
    // ------------------------------------------------------------------

    @GetMapping("/film")
    public String getFilm() {
        return "film/lista";
    }

    /**
     * Dettaglio del film: informazioni, regista, cast con i personaggi interpretati,
     * festival ai quali partecipa, proiezioni programmate e recensioni.
     */
    @GetMapping("/film/{id}")
    public String getFilm(@PathVariable Long id, Model model) {
        Film film = filmService.findById(id);
        model.addAttribute("film", film);
        model.addAttribute("cast", filmService.getCast(id));
        model.addAttribute("proiezioni", proiezioneService.findByFilm(id));
        model.addAttribute("recensioni", recensioneService.findByFilm(id));
        model.addAttribute("mediaVoto", recensioneService.mediaVoto(id));
        model.addAttribute("conteggioRecensioni", recensioneService.conteggio(id));

        // Recensione dell'utente collegato per questo film (se ne ha una):
        // abilita i comandi di modifica/eliminazione solo sul proprio contributo.
        User utente = utenteCorrente();
        Long utenteId = utente != null ? utente.getId() : null;
        model.addAttribute("miaRecensione",
                utenteId != null ? recensioneService.findByFilmAndUtente(id, utenteId) : null);
        model.addAttribute("puoRecensire", recensioneService.puoRecensire(id, utenteId));
        return "film/dettaglio";
    }

    // ------------------------------------------------------------------
    // Funzionalita' riservate all'amministratore
    // ------------------------------------------------------------------

    @GetMapping("/admin/film/nuovo")
    public String showNuovoFilmForm(Model model) {
        model.addAttribute("film", new Film());
        model.addAttribute("generi", Film.Genere.values());
        return "admin/nuovoFilm";
    }

    @PostMapping("/admin/film/nuovo")
    public String saveFilm(@Valid @ModelAttribute("film") Film film, BindingResult bindingResult, Model model) {
        model.addAttribute("generi", Film.Genere.values());
        if (bindingResult.hasErrors()) {
            return "admin/nuovoFilm";
        }
        filmService.save(film);
        return "redirect:/film";
    }

    @GetMapping("/admin/film/{id}/modifica")
    public String showModificaFilmForm(@PathVariable Long id, Model model) {
        model.addAttribute("film", filmService.findById(id));
        model.addAttribute("generi", Film.Genere.values());
        return "admin/modificaFilm";
    }

    @PostMapping("/admin/film/{id}/modifica")
    public String modificaFilm(@PathVariable Long id,
                               @Valid @ModelAttribute("film") Film film,
                               BindingResult bindingResult, Model model) {
        model.addAttribute("generi", Film.Genere.values());
        if (bindingResult.hasErrors()) {
            return "admin/modificaFilm";
        }
        filmService.modifica(id, film);
        return "redirect:/film/" + id;
    }

    /**
     * Gestione del cast: elenco delle interpretazioni e form per aggiungerne una.
     */
    @GetMapping("/admin/film/{id}/cast")
    public String showCastForm(@PathVariable Long id, Model model) {
        model.addAttribute("film", filmService.findById(id));
        model.addAttribute("cast", filmService.getCast(id));
        model.addAttribute("attori", attoreService.findAll());
        return "film/cast";
    }

    @PostMapping("/admin/film/{id}/cast")
    public String aggiungiInterpretazione(@PathVariable Long id,
                                          @RequestParam(value = "attoreId", required = false) Long attoreId,
                                          @RequestParam(value = "personaggio", required = false) String personaggio,
                                          RedirectAttributes redirectAttributes) {
        try {
            filmService.aggiungiInterpretazione(id, attoreId, personaggio);
            redirectAttributes.addFlashAttribute("successo", "Attore aggiunto al cast.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errore", e.getMessage());
        }
        return "redirect:/admin/film/" + id + "/cast";
    }

    @PostMapping("/admin/film/{id}/cast/{interpretazioneId}/elimina")
    public String rimuoviInterpretazione(@PathVariable Long id,
                                         @PathVariable Long interpretazioneId,
                                         RedirectAttributes redirectAttributes) {
        try {
            filmService.rimuoviInterpretazione(id, interpretazioneId);
            redirectAttributes.addFlashAttribute("successo", "Interpretazione rimossa dal cast.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errore", e.getMessage());
        }
        return "redirect:/admin/film/" + id + "/cast";
    }

    private User utenteCorrente() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Credentials credentials = credentialService.getCredentials(authentication.getName());
        return credentials != null ? credentials.getUser() : null;
    }
}
