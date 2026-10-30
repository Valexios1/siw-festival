package it.uniroma3.siw.festival.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.festival.model.Attore;
import it.uniroma3.siw.festival.service.AttoreService;
import jakarta.validation.Valid;

/**
 * Gestione degli attori: tutte le funzionalita' sono riservate all'amministratore.
 */
@Controller
public class AttoreController {

    @Autowired
    private AttoreService attoreService;

    @GetMapping("/admin/attori")
    public String getAttori(Model model) {
        model.addAttribute("attori", attoreService.findAll());
        return "admin/attori";
    }

    @GetMapping("/admin/attore/nuovo")
    public String showNuovoAttoreForm(Model model) {
        model.addAttribute("attore", new Attore());
        return "admin/nuovoAttore";
    }

    @PostMapping("/admin/attore/nuovo")
    public String saveAttore(@Valid @ModelAttribute("attore") Attore attore, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/nuovoAttore";
        }
        try {
            attoreService.save(attore);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("errore", e.getMessage());
            return "admin/nuovoAttore";
        }
        return "redirect:/admin/attori";
    }

    @GetMapping("/admin/attore/{id}/modifica")
    public String showModificaAttoreForm(@PathVariable Long id, Model model) {
        model.addAttribute("attore", attoreService.findById(id));
        return "admin/modificaAttore";
    }

    @PostMapping("/admin/attore/{id}/modifica")
    public String modificaAttore(@PathVariable Long id,
                                 @Valid @ModelAttribute("attore") Attore attore,
                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/modificaAttore";
        }
        try {
            attoreService.modifica(id, attore);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("errore", e.getMessage());
            return "admin/modificaAttore";
        }
        return "redirect:/admin/attori";
    }
}
