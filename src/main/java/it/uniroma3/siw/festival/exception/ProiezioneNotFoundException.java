package it.uniroma3.siw.festival.exception;

public class ProiezioneNotFoundException extends RuntimeException {

    public ProiezioneNotFoundException(Long id) {
        super("Proiezione con id " + id + " non trovata.");
    }
}
