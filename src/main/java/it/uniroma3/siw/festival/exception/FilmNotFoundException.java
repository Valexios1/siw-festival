package it.uniroma3.siw.festival.exception;

public class FilmNotFoundException extends RuntimeException {

    public FilmNotFoundException(Long id) {
        super("Film con id " + id + " non trovato.");
    }
}
