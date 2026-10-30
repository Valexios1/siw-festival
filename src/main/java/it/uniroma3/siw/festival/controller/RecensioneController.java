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

import it.uniroma3.siw.festival.exception.RecensioneNotFoundException;
import it.uniroma3.siw.festival.model.Credentials;
import it.uniroma3.siw.festival.model.Film;
import it.uniroma3.siw.festival.model.Recensione;
import it.uniroma3.siw.festival.model.User;
import it.uniroma3.siw.festival.service.CredentialService;
import it.uniroma3.siw.festival.service.FilmService;
import it.uniroma3.siw.festival.service.RecensioneService;
import jakarta.validation.Valid;

@Controller
public class RecensioneController {

    @Autowired
    private RecensioneService recensioneService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private CredentialService credentialService;

    /**
     * Nuova recensione per il film indicato. Se l'utente ha gia' recensito il film
     * viene portato direttamente sulla modifica della recensione esistente.
     */
    @GetMapping("/film/{filmId}/recensioni/nuova")
    public String nuovaForm(@PathVariable Long filmId, Model model) {
        User utente = utenteCorrente();
        Film film = filmService.findById(filmId);

        Recensione esistente = recensioneService.findByFilmAndUtente(filmId, utente.getId());
        if (esistente != null) {
            return "redirect:/recensioni/" + esistente.getId() + "/modifica";
        }

        Recensione recensione = new Recensione();
        recensione.setFilm(film);
        model.addAttribute("recensione", recensione);
        return "recensione/form";
    }

    @PostMapping("/film/{filmId}/recensioni/nuova")
    public String nuova(@PathVariable Long filmId,
                        @Valid @ModelAttribute("recensione") Recensione recensione,
                        BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        User utente = utenteCorrente();
        Film film = filmService.findById(filmId);
        recensione.setFilm(film);

        if (bindingResult.hasErrors()) {
            return "recensione/form";
        }

        try {
            recensione.setUtente(utente);
            recensioneService.salva(recensione);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("errore", e.getMessage());
            return "recensione/form";
        }

        redirectAttributes.addFlashAttribute("successo", "Recensione salvata.");
        return "redirect:/film/" + filmId;
    }

    @GetMapping("/recensioni/{id}/modifica")
    public String modificaForm(@PathVariable Long id, Model model) {
        model.addAttribute("recensione", caricaMiaRecensione(id));
        return "recensione/form";
    }

    @PostMapping("/recensioni/{id}/modifica")
    public String modifica(@PathVariable Long id,
                           @Valid @ModelAttribute("recensione") Recensione recensione,
                           BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        Recensione esistente = caricaMiaRecensione(id);

        if (bindingResult.hasErrors()) {
            // Mantiene film e id per far restare il form in modalita' "modifica".
            recensione.setId(id);
            recensione.setFilm(esistente.getFilm());
            return "recensione/form";
        }

        recensioneService.modifica(id, recensione);
        redirectAttributes.addFlashAttribute("successo", "Recensione aggiornata.");
        return "redirect:/film/" + esistente.getFilm().getId();
    }

    /**
     * Eliminazione: consentita all'autore della recensione e all'amministratore
     * (che opera dalla pagina di moderazione).
     */
    @PostMapping("/recensioni/{id}/elimina")
    public String elimina(@PathVariable Long id,
                          @RequestParam(value = "filmId", required = false) Long filmId,
                          @RequestParam(value = "admin", required = false, defaultValue = "false") boolean daAdmin,
                          RedirectAttributes redirectAttributes) {
        Recensione recensione = recensioneService.findById(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User utente = utenteCorrente();
        boolean autore = utente != null && recensione.getUtente() != null
                && recensione.getUtente().getId().equals(utente.getId());

        if (!autore && !isAdmin(authentication)) {
            // Ne' autore ne' amministratore: la recensione risulta inesistente.
            throw new RecensioneNotFoundException(id);
        }

        Long filmDellaRecensione = recensione.getFilm().getId();
        recensioneService.elimina(id);

        redirectAttributes.addFlashAttribute("successo", "Recensione eliminata.");
        if (daAdmin) {
            return "redirect:/admin/recensioni";
        }
        return "redirect:/film/" + (filmId != null ? filmId : filmDellaRecensione);
    }

    /**
     * Elenco di tutte le recensioni (moderazione). URL sotto /admin/**.
     */
    @GetMapping("/admin/recensioni")
    public String getRecensioniAdmin(Model model) {
        model.addAttribute("recensioni", recensioneService.findAll());
        return "admin/recensioni";
    }

    /**
     * Carica la recensione verificando che sia dell'utente corrente: un utente puo'
     * modificare o eliminare esclusivamente le proprie recensioni.
     */
    private Recensione caricaMiaRecensione(Long id) {
        Recensione recensione = recensioneService.findById(id);
        User utente = utenteCorrente();

        if (utente == null || recensione.getUtente() == null
                || !recensione.getUtente().getId().equals(utente.getId())) {
            throw new RecensioneNotFoundException(id);
        }
        return recensione;
    }

    private User utenteCorrente() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Credentials credentials = credentialService.getCredentials(authentication.getName());
        return credentials != null ? credentials.getUser() : null;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(Credentials.ADMIN_ROLE));
    }
}
