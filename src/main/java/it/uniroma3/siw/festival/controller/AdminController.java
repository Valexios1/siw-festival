package it.uniroma3.siw.festival.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Pannello di amministrazione: punto di accesso alle funzionalita' di gestione.
 */
@Controller
public class AdminController {

    @GetMapping("/admin")
    public String getPannelloAdmin() {
        return "admin/dashboard";
    }
}
