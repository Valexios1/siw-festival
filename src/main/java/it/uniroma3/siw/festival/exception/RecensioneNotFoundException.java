package it.uniroma3.siw.festival.exception;

public class RecensioneNotFoundException extends RuntimeException {

    public RecensioneNotFoundException(Long id) {
        super("Recensione con id " + id + " non trovata.");
    }
}
