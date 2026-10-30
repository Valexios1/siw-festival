package it.uniroma3.siw.festival.exception;

public class FestivalNotFoundException extends RuntimeException {

    public FestivalNotFoundException(Long id) {
        super("Festival con id " + id + " non trovato.");
    }
}
