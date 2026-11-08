package it.uniroma3.siw.festival.exception;

public class CredentialsNotFoundException extends RuntimeException {

    public CredentialsNotFoundException(Long id) {
        super("Credenziali con id " + id + " non trovate.");
    }

    public CredentialsNotFoundException(String username) {
        super("Credenziali per l'utente " + username + " non trovate.");
    }
}
