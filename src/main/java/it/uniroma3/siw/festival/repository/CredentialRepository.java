package it.uniroma3.siw.festival.repository;

import it.uniroma3.siw.festival.model.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialRepository extends JpaRepository<Credentials, Long> {

    public Optional<Credentials> findByUsername(String username);

}
