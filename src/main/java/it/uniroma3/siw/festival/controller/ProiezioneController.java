package it.uniroma3.siw.festival.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.festival.model.Proiezione;
import it.uniroma3.siw.festival.service.FestivalService;
import it.uniroma3.siw.festival.service.FilmService;
import it.uniroma3.siw.festival.service.ProiezioneService;
import it.uniroma3.siw.festival.service.SalaService;
import jakarta.validation.Valid;

@Controller
public class ProiezioneController {

    @Autowired
    private ProiezioneService proiezioneService;

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private SalaService salaService;

    /**
     * Programma delle proiezioni: pubblica per tutti gli utenti, con i comandi di
     * gestione visibili solo all'amministratore.
     */
    @GetMapping("/proiezioni")
    public String getProiezioni(Model model) {
        model.addAttribute("proiezioni", proiezioneService.findAll());
        model.addAttribute("stati", Proiezione.Stato.values());
        return "proiezione/lista";
    }

    // ------------------------------------------------------------------
    // Funzionalita' riservate all'amministratore
    // ------------------------------------------------------------------

    @GetMapping("/admin/proiezione/nuova")
    public String showNuovaProiezioneForm(Model model) {
        model.addAttribute("proiezione", new Proiezione());
        model.addAttribute("stati", Proiezione.Stato.values());
        aggiungiListeAlModello(model);
        return "admin/nuovoProiezione";
    }

    @PostMapping("/admin/proiezione/nuova")
    public String programmaProiezione(@Valid @ModelAttribute("proiezione") Proiezione proiezione,
                                      BindingResult bindingResult, Model model) {
        model.addAttribute("stati", Proiezione.Stato.values());
        aggiungiListeAlModello(model);

        if (bindingResult.hasErrors()) {
            return "admin/nuovoProiezione";
        }
        try {
            proiezioneService.programmaProiezione(proiezione);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("errore", e.getMessage());
            return "admin/nuovoProiezione";
        }
        return "redirect:/proiezioni";
    }

    @GetMapping("/admin/proiezione/{id}/modifica")
    public String showModificaProiezioneForm(@PathVariable Long id, Model model) {
        model.addAttribute("proiezione", proiezioneService.findById(id));
        model.addAttribute("stati", Proiezione.Stato.values());
        aggiungiListeAlModello(model);
        return "admin/modificaProiezione";
    }

    @PostMapping("/admin/proiezione/{id}/modifica")
    public String modificaProiezione(@PathVariable Long id,
                                     @Valid @ModelAttribute("proiezione") Proiezione proiezione,
                                     BindingResult bindingResult, Model model) {
        model.addAttribute("stati", Proiezione.Stato.values());
        aggiungiListeAlModello(model);

        if (bindingResult.hasErrors()) {
            return "admin/modificaProiezione";
        }
        try {
            proiezioneService.modificaProiezione(id, proiezione);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("errore", e.getMessage());
            return "admin/modificaProiezione";
        }
        return "redirect:/proiezioni";
    }

    /**
     * Aggiornamento rapido dello stato della proiezione dalla pagina del programma.
     */
    @PostMapping("/admin/proiezione/{id}/stato")
    public String cambiaStato(@PathVariable Long id,
                              @RequestParam("stato") Proiezione.Stato stato,
                              RedirectAttributes redirectAttributes) {
        proiezioneService.cambiaStato(id, stato);
        redirectAttributes.addFlashAttribute("successo", "Stato della proiezione aggiornato.");
        return "redirect:/proiezioni";
    }

    @PostMapping("/admin/proiezione/{id}/elimina")
    public String eliminaProiezione(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        proiezioneService.eliminaProiezione(id);
        redirectAttributes.addFlashAttribute("successo", "Proiezione eliminata.");
        return "redirect:/proiezioni";
    }

    private void aggiungiListeAlModello(Model model) {
        model.addAttribute("festival", festivalService.findAll());
        model.addAttribute("film", filmService.findAll());
        model.addAttribute("sale", salaService.findAll());
    }
}
