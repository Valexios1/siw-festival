package it.uniroma3.siw.festival.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.festival.model.Festival;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    /*
     * Elenco dei festival, dal piu' recente al piu' vecchio: e' l'ordine con cui
     * vengono mostrati nella pagina pubblica.
     */
    List<Festival> findAllByOrderByAnnoDesc();

    /*
     * Il festival si carica sempre insieme ai film partecipanti: la findById e' usata
     * dal dettaglio pubblico, dalla pagina di associazione dei film e dalla modifica,
     * e la collezione e' piccola.
     *
     * L'EntityGraph sostituisce il "left join fetch" che era scritto in JPQL: i film
     * arrivano con la stessa query del festival, senza una query per ogni film
     * mostrato in pagina (problema N+1).
     */
    @Override
    @EntityGraph(attributePaths = { "film" })
    Optional<Festival> findById(Long id);
}
