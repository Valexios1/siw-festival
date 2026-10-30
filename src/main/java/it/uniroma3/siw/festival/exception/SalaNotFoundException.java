package it.uniroma3.siw.festival.exception;

public class SalaNotFoundException extends RuntimeException {

    public SalaNotFoundException(Long id) {
        super("Sala con id " + id + " non trovata.");
    }
}
