package it.uniroma3.siw.festival.model;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

@Entity
public class Proiezione {

    public enum Stato {
        PROGRAMMATA, COMPLETATA, ANNULLATA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Data e ora di inizio della proiezione (rappresenta sia la "data" sia l'"ora" della proiezione).
     */
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(nullable = false)
    private LocalDateTime dataOra;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stato stato = Stato.PROGRAMMATA;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Festival festival;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Film film;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Sala sala;

    /**
     * Istante di fine della proiezione, calcolato dalla durata del film.
     * Non e' una colonna: e' un valore derivato, usato per il controllo di
     * sovrapposizione tra proiezioni nella stessa sala.
     */
    public LocalDateTime getDataOraFine() {
        if (this.dataOra == null || this.film == null || this.film.getDurata() == null) {
            return null;
        }
        return this.dataOra.plusMinutes(this.film.getDurata());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public void setDataOra(LocalDateTime dataOra) {
        this.dataOra = dataOra;
    }

    public Stato getStato() {
        return stato;
    }

    public void setStato(Stato stato) {
        this.stato = stato;
    }

    public Festival getFestival() {
        return festival;
    }

    public void setFestival(Festival festival) {
        this.festival = festival;
    }

    public Film getFilm() {
        return film;
    }

    public void setFilm(Film film) {
        this.film = film;
    }

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Proiezione that = (Proiezione) o;
        return Objects.equals(id, that.id) && Objects.equals(dataOra, that.dataOra)
                && Objects.equals(sala, that.sala) && Objects.equals(film, that.film);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dataOra, sala, film);
    }
}
