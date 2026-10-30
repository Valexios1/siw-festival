package it.uniroma3.siw.festival.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.festival.model.Attore;

public interface AttoreRepository extends CrudRepository<Attore, Long> {

    List<Attore> findAllByOrderByCognomeAscNomeAsc();
}
