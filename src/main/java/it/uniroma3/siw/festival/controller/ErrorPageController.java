package it.uniroma3.siw.festival.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ErrorPageController {

    @GetMapping("/error/403")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accessoNegato() {
        return "error/403";
    }

    @GetMapping("/error/400")
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String richiestaNonValida() {
        return "error/400";
    }
}
