package it.uniroma3.siw.festival.model;

import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
public class Film {

    public enum Genere {
        AZIONE, AVVENTURA, ANIMAZIONE, COMMEDIA, DOCUMENTARIO, DRAMMATICO,
        FANTASCIENZA, FANTASY, HORROR, MUSICAL, ROMANTICO, STORICO, THRILLER, WESTERN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String titolo;

    @NotNull
    @Column(nullable = false)
    private Integer anno;

    /**
     * Durata in minuti: serve anche per calcolare l'intervallo occupato da una proiezione.
     */
    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer durata;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genere genere;

    @NotBlank
    @Column(nullable = false)
    private String paeseProduzione;

    @NotBlank
    @Column(nullable = false)
    private String regista;

    /*
     * Festival ai quali il film partecipa: lato inverso della relazione molti-a-molti
     * proprietaria di Festival.film.
     */
    @ManyToMany(mappedBy = "film")
    private List<Festival> festival;

    /*
     * Attori che recitano nel film, con il personaggio interpretato: la relazione
     * molti-a-molti con Attore e' modellata dall'entita' Interpretazione perche'
     * porta un attributo proprio ("personaggio").
     */
    @OneToMany(mappedBy = "film")
    private List<Interpretazione> cast;

    @OneToMany(mappedBy = "film")
    private List<Proiezione> proiezioni;

    @OneToMany(mappedBy = "film")
    private List<Recensione> recensioni;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public Integer getAnno() {
        return anno;
    }

    public void setAnno(Integer anno) {
        this.anno = anno;
    }

    public Integer getDurata() {
        return durata;
    }

    public void setDurata(Integer durata) {
        this.durata = durata;
    }

    public Genere getGenere() {
        return genere;
    }

    public void setGenere(Genere genere) {
        this.genere = genere;
    }

    public String getPaeseProduzione() {
        return paeseProduzione;
    }

    public void setPaeseProduzione(String paeseProduzione) {
        this.paeseProduzione = paeseProduzione;
    }

    public String getRegista() {
        return regista;
    }

    public void setRegista(String regista) {
        this.regista = regista;
    }

    public List<Festival> getFestival() {
        return festival;
    }

    public void setFestival(List<Festival> festival) {
        this.festival = festival;
    }

    public List<Interpretazione> getCast() {
        return cast;
    }

    public void setCast(List<Interpretazione> cast) {
        this.cast = cast;
    }

    public List<Proiezione> getProiezioni() {
        return proiezioni;
    }

    public void setProiezioni(List<Proiezione> proiezioni) {
        this.proiezioni = proiezioni;
    }

    public List<Recensione> getRecensioni() {
        return recensioni;
    }

    public void setRecensioni(List<Recensione> recensioni) {
        this.recensioni = recensioni;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Film that = (Film) o;
        return Objects.equals(id, that.id) && Objects.equals(titolo, that.titolo) && Objects.equals(anno, that.anno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titolo, anno);
    }
}
