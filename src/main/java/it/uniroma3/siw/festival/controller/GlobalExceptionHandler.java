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
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * Gestione centralizzata degli errori: risorse inesistenti (404), richieste
 * non valide (400), accesso negato (403) e errori interni (500).
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

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResource(NoResourceFoundException ex) {
        return "error/404";
    }

    @ExceptionHandler({ IllegalArgumentException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Exception ex, Model model) {
        model.addAttribute("messaggio", ex.getMessage());
        return "error/400";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, Model model) {
        logger.error("Errore non gestito", ex);
        model.addAttribute("messaggio", ex.getMessage());
        return "error/500";
    }
}
