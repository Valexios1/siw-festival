package it.uniroma3.siw.festival.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.festival.model.Sala;
import it.uniroma3.siw.festival.service.SalaService;
import jakarta.validation.Valid;

/**
 * Gestione delle sale: tutte le funzionalita' sono riservate all'amministratore.
 */
@Controller
public class SalaController {

    @Autowired
    private SalaService salaService;

    @GetMapping("/admin/sale")
    public String getSale(Model model) {
        model.addAttribute("sale", salaService.findAll());
        return "admin/sale";
    }

    @GetMapping("/admin/sala/nuova")
    public String showNuovaSalaForm(Model model) {
        model.addAttribute("sala", new Sala());
        return "admin/nuovaSala";
    }

    @PostMapping("/admin/sala/nuova")
    public String saveSala(@Valid @ModelAttribute("sala") Sala sala, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/nuovaSala";
        }
        try {
            salaService.save(sala);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("errore", e.getMessage());
            return "admin/nuovaSala";
        }
        return "redirect:/admin/sale";
    }

    @GetMapping("/admin/sala/{id}/modifica")
    public String showModificaSalaForm(@PathVariable Long id, Model model) {
        model.addAttribute("sala", salaService.findById(id));
        return "admin/modificaSala";
    }

    @PostMapping("/admin/sala/{id}/modifica")
    public String modificaSala(@PathVariable Long id,
                               @Valid @ModelAttribute("sala") Sala sala,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/modificaSala";
        }
        try {
            salaService.modifica(id, sala);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("errore", e.getMessage());
            return "admin/modificaSala";
        }
        return "redirect:/admin/sale";
    }
}
