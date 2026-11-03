package it.uniroma3.siw.festival.repository;

import it.uniroma3.siw.festival.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
