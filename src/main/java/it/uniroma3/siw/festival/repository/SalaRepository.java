package it.uniroma3.siw.festival.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.festival.model.Sala;

public interface SalaRepository extends CrudRepository<Sala, Long> {

    List<Sala> findAllByOrderByNomeAsc();

    boolean existsByNome(String nome);
}
