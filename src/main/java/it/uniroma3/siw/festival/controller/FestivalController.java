package it.uniroma3.siw.festival.controller;

import java.util.List;

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

import it.uniroma3.siw.festival.model.Festival;
import it.uniroma3.siw.festival.model.Film;
import it.uniroma3.siw.festival.service.FestivalService;
import it.uniroma3.siw.festival.service.FilmService;
import it.uniroma3.siw.festival.service.ProiezioneService;
import jakarta.validation.Valid;

@Controller
public class FestivalController {

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private ProiezioneService proiezioneService;

    // ------------------------------------------------------------------
    // Funzionalita' pubbliche
    // ------------------------------------------------------------------

    @GetMapping("/festival")
    public String getFestival(Model model) {
        model.addAttribute("festival", festivalService.findAll());
        return "festival/lista";
    }

    /**
     * Dettaglio del festival: informazioni, film partecipanti e programma delle
     * proiezioni, entrambi raggiungibili da questa pagina.
     */
    @GetMapping("/festival/{id}")
    public String getFestival(@PathVariable Long id, Model model) {
        model.addAttribute("festival", festivalService.findByIdConFilm(id));
        model.addAttribute("proiezioni", proiezioneService.findByFestival(id));
        return "festival/dettaglio";
    }

    // ------------------------------------------------------------------
    // Funzionalita' riservate all'amministratore
    // ------------------------------------------------------------------

    @GetMapping("/admin/festival/nuovo")
    public String showNuovoFestivalForm(Model model) {
        model.addAttribute("festival", new Festival());
        return "admin/nuovoFestival";
    }

    @PostMapping("/admin/festival/nuovo")
    public String saveFestival(@Valid @ModelAttribute("festival") Festival festival,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/nuovoFestival";
        }
        try {
            festivalService.save(festival);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("errore", e.getMessage());
            return "admin/nuovoFestival";
        }
        return "redirect:/festival";
    }

    @GetMapping("/admin/festival/{id}/modifica")
    public String showModificaFestivalForm(@PathVariable Long id, Model model) {
        model.addAttribute("festival", festivalService.findById(id));
        return "admin/modificaFestival";
    }

    @PostMapping("/admin/festival/{id}/modifica")
    public String modificaFestival(@PathVariable Long id,
                                   @Valid @ModelAttribute("festival") Festival festival,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/modificaFestival";
        }
        try {
            festivalService.modifica(id, festival);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("errore", e.getMessage());
            return "admin/modificaFestival";
        }
        return "redirect:/festival/" + id;
    }

    /**
     * Associazione film-festival: la selezione inviata sostituisce quella corrente,
     * quindi da questa pagina si aggiungono e si tolgono film dal festival.
     */
    @GetMapping("/admin/festival/{id}/film")
    public String showAssociaFilmForm(@PathVariable Long id, Model model) {
        Festival festival = festivalService.findByIdConFilm(id);
        model.addAttribute("festival", festival);
        model.addAttribute("film", filmService.findAll());
        model.addAttribute("filmSelezionatiIds",
                festival.getFilm() == null ? List.of()
                        : festival.getFilm().stream().map(Film::getId).toList());
        return "festival/film";
    }

    @PostMapping("/admin/festival/{id}/film")
    public String associaFilm(@PathVariable Long id,
                              @RequestParam(value = "filmIds", required = false) List<Long> filmIds,
                              RedirectAttributes redirectAttributes) {
        festivalService.associaFilm(id, filmIds);
        redirectAttributes.addFlashAttribute("successo", "Film associati al festival.");
        return "redirect:/festival/" + id;
    }
}
