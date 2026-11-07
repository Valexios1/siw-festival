package it.uniroma3.siw.festival.DTO;

import it.uniroma3.siw.festival.model.Film;

public record FilmDTO(Long id, String titolo, Integer anno, Integer durata,
                      String genere, String paeseProduzione, String regista) {

    public static FilmDTO da(Film film) {
        return new FilmDTO(
                film.getId(),
                film.getTitolo(),
                film.getAnno(),
                film.getDurata(),
                film.getGenere() != null ? film.getGenere().name() : null,
                film.getPaeseProduzione(),
                film.getRegista());
    }
}
