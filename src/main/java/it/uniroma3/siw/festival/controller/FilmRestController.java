package it.uniroma3.siw.festival.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.siw.festival.DTO.FilmDTO;
import it.uniroma3.siw.festival.model.Film;
import it.uniroma3.siw.festival.service.FilmService;

@RestController
@RequestMapping("/api/film")
public class FilmRestController {

    @Autowired
    private FilmService filmService;

    @GetMapping
    public List<FilmDTO> cerca(@RequestParam(required = false) Film.Genere genere,
                               @RequestParam(required = false) String titolo) {
        return filmService.cerca(genere, titolo).stream().map(FilmDTO::da).toList();
    }

    @GetMapping("/generi")
    public List<String> generi() {
        return Arrays.stream(Film.Genere.values()).map(Enum::name).toList();
    }
}
