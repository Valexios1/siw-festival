package it.uniroma3.siw.festival.exception;

public class AttoreNotFoundException extends RuntimeException {

    public AttoreNotFoundException(Long id) {
        super("Attore con id " + id + " non trovato.");
    }
}
