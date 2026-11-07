# Siw Festival - note sul progetto

Sistema informativo per la gestione di festival cinematografici
(Spring Boot 4, JPA/Hibernate, PostgreSQL, Thymeleaf, Spring Security).

## 1. Modello dati

| Entità | Attributi principali | Relazioni |
|---|---|---|
| **Festival** | nome, anno, citta, dataInizio, dataFine, descrizione | `@ManyToMany` verso Film (lato proprietario, tabella `festival_film`); `@OneToMany` verso Proiezione |
| **Film** | titolo, anno, durata (minuti), genere (enum), paeseProduzione, regista | `@ManyToMany` verso Festival (lato inverso); `@OneToMany` verso Interpretazione, Proiezione, Recensione |
| **Attore** | nome, cognome, dataNascita, nazionalita | `@OneToMany` verso Interpretazione |
| **Interpretazione** | personaggio | `@ManyToOne` verso Film, `@ManyToOne` verso Attore |
| **Sala** | nome, indirizzo, capienza | `@OneToMany` verso Proiezione |
| **Proiezione** | dataOra, stato (PROGRAMMATA/COMPLETATA/ANNULLATA) | `@ManyToOne` verso Festival, Film, Sala |
| **Recensione** | voto (1-5), testo, dataCreazione | `@ManyToOne` verso Film, `@ManyToOne` verso User |
| **User** | name, surname, email, phoneNumber, nation | `@OneToOne` (lato inverso di Credentials) |
| **Credentials** | username, password (BCrypt), role (USER/ADMIN) | `@OneToOne` verso User |

Relazioni richieste dai requisiti minimi:

- **molti-a-molti**: Festival ↔ Film (tabella di join `festival_film`);
- **uno-a-molti / molti-a-uno**: Festival → Proiezione, Film → Proiezione, Sala → Proiezione,
  Film → Recensione, User → Recensione, Film → Interpretazione;
- **molti-a-molti con attributo**: Film ↔ Attore, modellata dall'entità **Interpretazione**
  perché la relazione porta il *personaggio* interpretato (es. Marlon Brando → "Don Vito Corleone").
  Una `@ManyToMany` pura non permetterebbe di salvare quell'attributo.

## 2. Strategie di fetch adottate e motivazioni

L'impostazione di base è **tutte le associazioni LAZY** (le `@ManyToOne` lo sono esplicitamente,
le collezioni lo sono per default) e **fetch mirati** nei casi d'uso che mostrano i dati collegati:

| Associazione | Strategia | Motivazione |
|---|---|---|
| `Proiezione.film`, `Proiezione.sala`, `Proiezione.festival` | LAZY + `@EntityGraph` nelle query del programma | Il programma è l'elenco più frequentato: un `EntityGraph` su `{film, sala, festival}` evita una query per riga (N+1), ma la stessa associazione resta LAZY quando la proiezione viene caricata per altri motivi (es. controllo di disponibilità) |
| `Festival.film` | LAZY + `@EntityGraph` sulla `findById` | Il festival si carica sempre con i film partecipanti (dettaglio, associazione dei film, modifica): il grafo evita una query per film. La collezione resta LAZY nell'elenco dei festival, dove i film non servono |
| `Film.festival` / `Film.cast` | LAZY + `@EntityGraph` sulla `findById` (`festival`) e `join fetch` nella query del cast | Il dettaglio film mostra entrambe le collezioni, ma Hibernate **non consente il fetch di due "bag" nella stessa query** (`MultipleBagFetchException`): le due collezioni viaggiano su due query |
| `Recensione.utente`, `Recensione.film` | LAZY + `@EntityGraph` | Le recensioni sono mostrate insieme all'autore (e al film) nella home, nel dettaglio film, in area personale e nella moderazione |
| `Interpretazione.attore` | LAZY + `join fetch` nella query del cast | Il cast è sempre mostrato con il nome dell'attore |

Nessuna associazione è mappata EAGER: il caricamento anticipato viene richiesto caso per caso
sul repository, tramite `@EntityGraph`, che è la scelta più controllabile (l'EAGER globale tende a
propagarsi su tutte le query e ad appesantire operazioni che non ne hanno bisogno).

Le strategie di fetch sono quindi espresse quasi interamente con `@EntityGraph`. Restano in JPQL due
`join fetch`: `ProiezioneRepository.findByFestivalIdConFilmESala` (programma del festival con film e
sala) e `InterpretazioneRepository.findByFilmIdOrderByAttoreCognomeAscAttoreNomeAsc` (cast con attori).

### Analisi sperimentale (sezione 8.2)

Il confronto è implementato nel test
`src/test/java/it/uniroma3/siw/festival/AnalisiAccessoDatiTest.java`.

Caso d'uso: caricamento di tutti i film del *Festival del Cinema di Roma* con le
relative proiezioni. Le tre strategie leggono lo stesso insieme di dati:

| Strategia | Come funziona | Query SQL |
|---|---|---|
| **LAZY** | una query per i film; `film.getProiezioni()` inizializza la collezione per ogni film | 1 + N |
| **JOIN FETCH** | `select distinct f ... left join fetch f.proiezioni` | 1 |
| **ENTITY GRAPH** | stessa query senza fetch, con `EntityGraph` sull'attributo `proiezioni` | 1 |

Per ogni strategia il test stampa film caricati, proiezioni caricate, numero di
query SQL (`Statistics.getPrepareStatementCount()`) e tempo complessivo. Ogni
strategia gira in una transazione separata (persistence context nuovo), le
statistiche vengono azzerate prima di ogni misura e un giro di warm-up precede
le misure.

Esecuzione (il test usa il PostgreSQL configurato in `application.properties`:
lo schema viene ricreato e i dati sono letti da `import.sql`, come all'avvio
dell'applicazione):

```bash
mvnw.cmd -Dtest=AnalisiAccessoDatiTest test   # Windows
./mvnw -Dtest=AnalisiAccessoDatiTest test     # Linux/macOS
```

Risultato con i dati di esempio (6 film; 7 proiezioni perché *La grande
bellezza* partecipa anche al Festival del Cinema Italiano):

```
Strategia: LAZY
Film caricati: 6
Proiezioni caricate: 7
Query SQL: 7
Tempo: 5.99 ms

Strategia: JOIN FETCH
Film caricati: 6
Proiezioni caricate: 7
Query SQL: 1
Tempo: 4.12 ms

Strategia: ENTITY GRAPH
Film caricati: 6
Proiezioni caricate: 7
Query SQL: 1
Tempo: 5.29 ms
```

Il confronto evidenzia l'N+1: LAZY esegue una query per ogni film (7 in totale),
mentre `join fetch` ed `EntityGraph` risolvono lo stesso caso con una sola query.
I tempi sono vicini perché il dataset è piccolo e il database è locale: la metrica
significativa è il numero di query, che cresce con il numero di film.


## 3. Gestione delle transazioni

- Tutte le letture sono annotate `@Transactional(readOnly = true)`: la transazione è in sola lettura,
  Hibernate non esegue il dirty checking al termine e il database può ottimizzare le letture.
- Tutti i casi d'uso di modifica sono `@Transactional` (propagazione di default `REQUIRED`).
- **Caso d'uso multi-entità**: `ProiezioneService.programmaProiezione(...)` recupera festival, film
  e sala, verifica che il film partecipi al festival e che la sala non sia occupata nello stesso
  intervallo, e solo allora salva la proiezione. L'operazione è atomica: qualunque verifica fallita
  solleva `IllegalArgumentException` e la transazione viene marcata per il rollback, quindi il
  sistema resta consistente.
- Livello di isolamento: viene usato il default **READ_COMMITTED**. Il controllo di disponibilità è
  un controllo applicativo e copre il caso d'uso normale; due transazioni concorrenti potrebbero in
  teoria superarlo entrambe (*write skew*) e inserire due proiezioni sovrapposte. Per escluderlo
  servirebbe un vincolo a livello di database (un `EXCLUDE` constraint sull'intervallo occupato)
  oppure `Isolation.SERIALIZABLE`, che farebbe fallire una delle due al commit. Il commento in
  `ProiezioneService` documenta questa scelta.

## 4. Sicurezza

- Autenticazione con form login e password cifrate con BCrypt (`SecurityConfiguration`).
- Ruoli: `USER` (utenti registrati) e `ADMIN` (amministratori), letti dalla tabella `credentials`
  tramite `JdbcUserDetailsManager`.
- Sono pubbliche solo le pagine di consultazione (`/`, `/festival/**`, `/film/**`, `/proiezioni`,
  `/login`, `/register`), le risorse statiche (`/css/**`, `/js/**`) e le API di lettura
  `/api/film/**`. Tutto ciò che sta sotto `/admin/**` richiede il ruolo ADMIN; le
  operazioni sulle recensioni e l'area personale richiedono un utente autenticato.
- Le recensioni sono protette anche a livello applicativo: `RecensioneController.caricaMiaRecensione`
  verifica che la recensione appartenga all'utente collegato e, in caso contrario, si comporta come
  se non esistesse (404). L'amministratore può eliminare qualsiasi recensione dalla pagina di
  moderazione.
- Un utente può scrivere **una sola recensione per film**: il vincolo è verificato in
  `RecensioneService.salva` (e la form di inserimento reindirizza alla modifica se la recensione
  esiste già).

## 5. Requisiti minimi: copertura

| Requisito | Dove |
|---|---|
| ≥ 6 entità significative | 9 entità (Festival, Film, Attore, Interpretazione, Sala, Proiezione, Recensione, User, Credentials) |
| 1-N, N-1 e almeno una M-N | Festival→Proiezione, Sala→Proiezione, Film→Recensione, Festival↔Film (M-N), Film↔Attore (M-N con attributo) |
| Casi d'uso pubblici | elenco festival, dettaglio festival (con film e proiezioni), dettaglio film (regista, cast, festival, proiezioni, recensioni), programma delle proiezioni |
| Frontend React + endpoint REST | widget React nella pagina `/film` (cartella `frontend/`) alimentato da `GET /api/film` e `GET /api/film/generi` |
| Casi d'uso utente registrato | inserimento, modifica ed eliminazione della propria recensione |
| Casi d'uso amministratore | festival (creazione/modifica), film (inserimento/modifica), associazione film-festival (e rimozione), sale (inserimento/modifica), programmazione/modifica/cancellazione proiezione, moderazione recensioni |
| Consistenza delle operazioni | nessuna proiezione in una sala già occupata nello stesso intervallo; il film deve partecipare al festival; una recensione per utente per film; il nome della sala è univoco; data di fine festival non precedente a quella di inizio |
| Autenticazione e autorizzazione | Spring Security + BCrypt, ruoli USER/ADMIN |
| Transazioni | `@Transactional` con `readOnly` sulle letture, caso d'uso multi-entità documentato |
| Validazione degli input | annotazioni Jakarta Validation sulle entità + `@Valid` nei controller + messaggi in pagina |
| Gestione degli errori | `GlobalExceptionHandler` → `error/404` e `error/500` |

## 6. Scelte escluse (e perché)

- **Gestione delle immagini**: esclusa su richiesta esplicita. Non esiste alcun servizio di
  caricamento immagini, nessuna cartella `static/images` e nessun riferimento a immagini nei
  template (il logo è testuale).
- **Resto del frontend**: le pagine diverse da `/film` restano server-side con Thymeleaf;
  React è usato solo per il widget con i filtri dei film, come richiesto.

## 7. Dati di esempio

`src/main/resources/import.sql` viene eseguito da Hibernate a ogni avvio con `ddl-auto=create`
(il database viene quindi ricreato). Contiene 3 festival, 12 film, 12 attori, 12 interpretazioni,
3 sale, 13 proiezioni e alcune recensioni. Account disponibili:

| Username | Password | Ruolo |
|---|---|---|
| `admin` | `admin` | ADMIN |
| `mario` | `admin` | USER |

## 8. Avvio

```bash
# una sola volta: creare il database
psql -U postgres -c "CREATE DATABASE festival;"

# compilare e avviare
mvnw clean compile
mvnw spring-boot:run

# frontend (solo se si modifica il widget; il bundle è già committato)
cd frontend
npm install
npm run build
```

L'applicazione risponde su `http://localhost:8080`. La connessione al database è configurata in
`src/main/resources/application.properties` (utente `postgres`, database `festival`).

## 9. Widget React e API REST

La pagina `/film` non renderizza più la lista lato server: contiene solo
`<div id="film-list-root"></div>` e carica il bundle React compilato
(`static/js/film-widget/film-widget.js`). Il widget filtra per titolo (con debounce)
e per genere, e ricava i dati dalle API pubbliche di sola lettura:

| Endpoint | Descrizione |
|---|---|
| `GET /api/film` | elenco film in ordine di titolo |
| `GET /api/film?titolo=...&genere=...` | filtro per titolo (contains, case-insensitive) e genere |
| `GET /api/film/generi` | valori di `Film.Genere` per la select |

Il codice sorgente del widget è in `frontend/` (Vite + React); il bundle compilato è
committato in `src/main/resources/static/js/film-widget/`, quindi l'applicazione non
richiede Node a runtime. In sviluppo `npm run dev` avvia Vite su `localhost:5173` e
inoltra `/api` a `localhost:8080`.
