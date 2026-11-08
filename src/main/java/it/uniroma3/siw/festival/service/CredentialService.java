package it.uniroma3.siw.festival.service;

import it.uniroma3.siw.festival.exception.CredentialsNotFoundException;
import it.uniroma3.siw.festival.model.Credentials;
import it.uniroma3.siw.festival.repository.CredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CredentialService {

    @Autowired
    private CredentialRepository credentialRepository;

    @Transactional(readOnly = true)
    public Credentials getCredentials(Long id) {
        return credentialRepository.findById(id).orElseThrow(() -> new CredentialsNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Credentials getCredentials(String username) {
        return credentialRepository.findByUsername(username)
                .orElseThrow(() -> new CredentialsNotFoundException(username));
    }

    @Transactional
    public Credentials saveCredentials(Credentials credentials) {
        return credentialRepository.save(credentials);
    }

}

