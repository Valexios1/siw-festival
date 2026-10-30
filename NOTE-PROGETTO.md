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
| `Festival.film` | LAZY + `left join fetch` in `findByIdConFilm` | Il dettaglio festival mostra i film partecipanti: un solo fetch evita una query per film. La collezione resta LAZY nell'elenco dei festival, dove i film non servono |
| `Film.festival` / `Film.cast` | LAZY + due query distinte (`findByIdConFestival`, `findByFilmIdConAttore`) | Il dettaglio film mostra entrambe le collezioni, ma Hibernate **non consente il fetch di due "bag" nella stessa query** (`MultipleBagFetchException`): la seconda collezione viene inizializzata con una query separata |
| `Recensione.utente`, `Recensione.film` | LAZY + `@EntityGraph` | Le recensioni sono mostrate insieme all'autore (e al film) nella home, nel dettaglio film, in area personale e nella moderazione |
| `Interpretazione.attore` | LAZY + `join fetch` | Il cast è sempre mostrato con il nome dell'attore |

Nessuna associazione è mappata EAGER: il caricamento anticipato viene richiesto caso per caso
sulla query, che è la scelta più controllabile (l'EAGER globale tende a propagarsi su tutte le query
e ad appesantire operazioni che non ne hanno bisogno).

### Analisi sperimentale (sezione 8.2)

`src/test/java/it/uniroma3/siw/festival/PrestazioniAccessoDatiTest.java` esegue lo stesso caso d'uso
(**programma di un festival con film e sale**) con tre strategie, stampando numero di query SQL,
entità caricate e tempo:

```bash
mvnw test -Dtest=PrestazioniAccessoDatiTest
```

1. **LAZY**: `findByFestivalIdOrderByDataOraAsc`; film e sala vengono inizializzati al primo accesso
   → 1 query per il programma + 1 per ogni film + 1 per ogni sala (N+1).
2. **JOIN FETCH**: `findByFestivalIdConFilmESala`, due `join fetch` nella stessa query → 1 query.
3. **@EntityGraph**: `findByFestivalIdOrderByDataOraAscConFilmESala`, stessa ottimizzazione dichiarata
   sul repository con `@EntityGraph(attributePaths = {"film", "sala"})` → 1 query.

Il test usa `hibernate.generate_statistics=true` (abilitato solo in quel test) e
`TransactionTemplate`: ogni strategia gira in una transazione dedicata, quindi con un persistence
context nuovo e senza riuso della first-level cache. Il database viene ricreato e ripopolato
all'avvio del contesto (`ddl-auto=create` + `import.sql`), quindi il confronto avviene sempre
sullo stesso insieme di dati.

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
  `/login`, `/register`). Tutto ciò che sta sotto `/admin/**` richiede il ruolo ADMIN; le
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
- **Parte React + API REST**: esclusa su richiesta esplicita. L'applicazione è interamente
  server-side con Thymeleaf. **Questa è l'unica parte dei requisiti minimi del documento non
  coperta** (sezione 9 e 10: almeno una parte del frontend in React e almeno un endpoint REST
  utilizzato dal frontend) e va segnalata fra i malfunzionamenti noti in fase di consegna.

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
```

L'applicazione risponde su `http://localhost:8080`. La connessione al database è configurata in
`src/main/resources/application.properties` (utente `postgres`, database `festival`).
