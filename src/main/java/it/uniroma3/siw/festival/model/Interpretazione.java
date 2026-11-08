package it.uniroma3.siw.festival.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Interpretazione di un personaggio da parte di un attore in un film.
 *
 * <p>E' l'entita' che realizza la relazione molti-a-molti tra Film e Attore:
 * la relazione non e' pura, ma porta l'attributo {@code personaggio}
 * (es. Marlon Brando interpreta don Vito Corleone nel Padrino), quindi non puo'
 * essere mappata con una semplice {@code @ManyToMany} + {@code @JoinTable}.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "film_id", "attore_id" }))
public class Interpretazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String personaggio;



    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Film film;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Attore attore;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPersonaggio() {
        return personaggio;
    }

    public void setPersonaggio(String personaggio) {
        this.personaggio = personaggio;
    }

    public Film getFilm() {
        return film;
    }

    public void setFilm(Film film) {
        this.film = film;
    }

    public Attore getAttore() {
        return attore;
    }

    public void setAttore(Attore attore) {
        this.attore = attore;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Interpretazione that = (Interpretazione) o;
        return Objects.equals(id, that.id) && Objects.equals(film, that.film) && Objects.equals(attore, that.attore)
                && Objects.equals(personaggio, that.personaggio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, film, attore, personaggio);
    }
}
