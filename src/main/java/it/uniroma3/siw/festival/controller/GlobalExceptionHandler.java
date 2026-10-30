package it.uniroma3.siw.festival.controller;

import it.uniroma3.siw.festival.exception.AttoreNotFoundException;
import it.uniroma3.siw.festival.exception.FestivalNotFoundException;
import it.uniroma3.siw.festival.exception.FilmNotFoundException;
import it.uniroma3.siw.festival.exception.ProiezioneNotFoundException;
import it.uniroma3.siw.festival.exception.RecensioneNotFoundException;
import it.uniroma3.siw.festival.exception.SalaNotFoundException;
import it.uniroma3.siw.festival.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Gestione centralizzata degli errori: le risorse inesistenti producono una
 * pagina 404 con il messaggio dell'eccezione, ogni altro errore una pagina 500.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({ FestivalNotFoundException.class, FilmNotFoundException.class,
            AttoreNotFoundException.class, SalaNotFoundException.class,
            ProiezioneNotFoundException.class, RecensioneNotFoundException.class,
            UserNotFoundException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(RuntimeException ex, Model model) {
        model.addAttribute("messaggio", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, Model model) {
        logger.error("Errore non gestito", ex);
        model.addAttribute("messaggio", ex.getMessage());
        return "error/500";
    }
}
