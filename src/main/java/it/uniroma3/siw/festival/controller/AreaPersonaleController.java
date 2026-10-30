package it.uniroma3.siw.festival.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import it.uniroma3.siw.festival.model.Credentials;
import it.uniroma3.siw.festival.model.User;
import it.uniroma3.siw.festival.service.CredentialService;
import it.uniroma3.siw.festival.service.RecensioneService;

@Controller
public class AreaPersonaleController {

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private RecensioneService recensioneService;

    /**
     * Area personale dell'utente registrato: le recensioni scritte, ognuna
     * modificabile ed eliminabile dal proprietario.
     */
    @GetMapping("/area-personale")
    public String getAreaPersonale(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Credentials credentials = credentialService.getCredentials(authentication.getName());
        User utente = credentials.getUser();

        model.addAttribute("utente", utente);
        model.addAttribute("recensioni", recensioneService.findByUtente(utente.getId()));
        return "areaPersonale/lista";
    }
}
