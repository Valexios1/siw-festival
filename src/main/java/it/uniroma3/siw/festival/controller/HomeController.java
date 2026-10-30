package it.uniroma3.siw.festival.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import it.uniroma3.siw.festival.service.FestivalService;
import it.uniroma3.siw.festival.service.RecensioneService;

@Controller
public class HomeController {

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private RecensioneService recensioneService;

    @GetMapping("/")
    public String getHome(Model model) {
        model.addAttribute("festival", festivalService.findAll());
        model.addAttribute("recensioni", recensioneService.ultime5());
        return "index";
    }
}
