package it.uniroma3.siw.festival.repository;

import it.uniroma3.siw.festival.model.Credentials;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CredentialRepository extends CrudRepository<Credentials, Long> {

    public Optional<Credentials> findByUsername(String username);

}
