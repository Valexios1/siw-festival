-- ============================================================
-- Dati di esempio, caricati da Hibernate all'avvio dell'applicazione
-- (spring.jpa.hibernate.ddl-auto=create -> lo schema viene ricreato
--  e questo script eseguito a ogni avvio).
--
-- NOTA: Hibernate esegue import.sql una riga per volta, quindi ogni
-- statement DEVE stare su una singola riga.
--
-- Account di prova:
--   admin / admin   (ruolo ADMIN)
--   mario / admin   (ruolo USER)   <- la password e' la stessa: l'hash
--                                     BCrypt e' quello dell'utente admin
-- ============================================================

-- ============================================================
-- Utenti e credenziali
-- users e credentials usano una sequenza (GenerationType.AUTO), quindi gli id
-- vanno indicati esplicitamente e le sequenze riallineate in fondo allo script.
-- ============================================================
INSERT INTO users (id, name, surname, email, phone_number, nation) VALUES (1, 'Amministratore', 'Sistema', 'admin@siwfestival.it', NULL, NULL);
INSERT INTO users (id, name, surname, email, phone_number, nation) VALUES (2, 'Mario', 'Rossi', 'mario.rossi@example.it', '+39 333 1112222', 'Italia');

INSERT INTO credentials (id, username, password, role, user_id) VALUES (1, 'admin', '$2a$10$CXiH0dLZy5pNCV4m9rlItOQnN2PoKQh5M.19J/42HEVG.N7HwMOV.', 'ADMIN', 1);
INSERT INTO credentials (id, username, password, role, user_id) VALUES (2, 'user', '$2a$10$CXiH0dLZy5pNCV4m9rlItOQnN2PoKQh5M.19J/42HEVG.N7HwMOV.', 'USER', 2);

-- Allinea le sequenze Hibernate agli id appena inseriti, cosi' i successivi
-- inserimenti (ad esempio una nuova registrazione) non collidono.
SELECT setval('users_seq', 2, true);
SELECT setval('credentials_seq', 2, true);

-- ============================================================
-- Festival
-- ============================================================
INSERT INTO festival (nome, anno, citta, data_inizio, data_fine, descrizione) VALUES ('Festival del Cinema di Roma', 2026, 'Roma', '2026-11-05', '2026-11-15', 'Rassegna internazionale dedicata al cinema d autore, con anteprime, retrospettive e incontri con registi e attori.');
INSERT INTO festival (nome, anno, citta, data_inizio, data_fine, descrizione) VALUES ('Torino Film Festival', 2026, 'Torino', '2026-11-20', '2026-11-28', 'Festival dedicato alle nuove tendenze del cinema indipendente e alle opere prime.');
INSERT INTO festival (nome, anno, citta, data_inizio, data_fine, descrizione) VALUES ('Festival del Cinema Italiano', 2025, 'Bologna', '2025-06-10', '2025-06-18', 'Edizione conclusa: retrospettiva sul cinema italiano dal dopoguerra agli anni duemila.');

-- ============================================================
-- Film
-- ============================================================
INSERT INTO film (titolo, anno, durata, genere, paese_produzione, regista) VALUES ('Il Padrino', 1972, 175, 'DRAMMATICO', 'Stati Uniti', 'Francis Ford Coppola');
INSERT INTO film (titolo, anno, durata, genere, paese_produzione, regista) VALUES ('La Dolce Vita', 1960, 174, 'DRAMMATICO', 'Italia', 'Federico Fellini');
INSERT INTO film (titolo, anno, durata, genere, paese_produzione, regista) VALUES ('Nuovo Cinema Paradiso', 1988, 155, 'DRAMMATICO', 'Italia', 'Giuseppe Tornatore');
INSERT INTO film (titolo, anno, durata, genere, paese_produzione, regista) VALUES ('La grande bellezza', 2013, 142, 'DRAMMATICO', 'Italia', 'Paolo Sorrentino');
INSERT INTO film (titolo, anno, durata, genere, paese_produzione, regista) VALUES ('Il buono, il brutto, il cattivo', 1966, 161, 'WESTERN', 'Italia', 'Sergio Leone');
INSERT INTO film (titolo, anno, durata, genere, paese_produzione, regista) VALUES ('Amarcord', 1973, 123, 'COMMEDIA', 'Italia', 'Federico Fellini');
INSERT INTO film (titolo, anno, durata, genere, paese_produzione, regista) VALUES ('Le fate ignoranti', 2001, 106, 'DRAMMATICO', 'Italia', 'Ferzan Ozpetek');
INSERT INTO film (titolo, anno, durata, genere, paese_produzione, regista) VALUES ('Parasite', 2019, 132, 'THRILLER', 'Corea del Sud', 'Bong Joon-ho');
INSERT INTO film (titolo, anno, durata, genere, paese_produzione, regista) VALUES ('Interstellar', 2014, 169, 'FANTASCIENZA', 'Stati Uniti', 'Christopher Nolan');
INSERT INTO film (titolo, anno, durata, genere, paese_produzione, regista) VALUES ('Le vite degli altri', 2006, 137, 'DRAMMATICO', 'Germania', 'Florian Henckel von Donnersmarck');
INSERT INTO film (titolo, anno, durata, genere, paese_produzione, regista) VALUES ('Pulp Fiction', 1994, 154, 'THRILLER', 'Stati Uniti', 'Quentin Tarantino');
INSERT INTO film (titolo, anno, durata, genere, paese_produzione, regista) VALUES ('Il labirinto del fauno', 2006, 118, 'FANTASY', 'Messico', 'Guillermo del Toro');

-- ============================================================
-- Attori
-- ============================================================
INSERT INTO attore (nome, cognome, data_nascita, nazionalita) VALUES ('Marlon', 'Brando', '1924-04-03', 'Stati Uniti');
INSERT INTO attore (nome, cognome, data_nascita, nazionalita) VALUES ('Al', 'Pacino', '1940-04-25', 'Stati Uniti');
INSERT INTO attore (nome, cognome, data_nascita, nazionalita) VALUES ('Marcello', 'Mastroianni', '1924-09-28', 'Italia');
INSERT INTO attore (nome, cognome, data_nascita, nazionalita) VALUES ('Anita', 'Ekberg', '1931-09-29', 'Svezia');
INSERT INTO attore (nome, cognome, data_nascita, nazionalita) VALUES ('Philippe', 'Noiret', '1930-10-01', 'Francia');
INSERT INTO attore (nome, cognome, data_nascita, nazionalita) VALUES ('Massimo', 'Troisi', '1953-02-19', 'Italia');
INSERT INTO attore (nome, cognome, data_nascita, nazionalita) VALUES ('Toni', 'Servillo', '1959-01-25', 'Italia');
INSERT INTO attore (nome, cognome, data_nascita, nazionalita) VALUES ('Clint', 'Eastwood', '1930-05-31', 'Stati Uniti');
INSERT INTO attore (nome, cognome, data_nascita, nazionalita) VALUES ('Eli', 'Wallach', '1915-12-07', 'Stati Uniti');
INSERT INTO attore (nome, cognome, data_nascita, nazionalita) VALUES ('John', 'Travolta', '1954-02-18', 'Stati Uniti');
INSERT INTO attore (nome, cognome, data_nascita, nazionalita) VALUES ('Bruno', 'Ganz', '1941-03-22', 'Svizzera');
INSERT INTO attore (nome, cognome, data_nascita, nazionalita) VALUES ('Roberto', 'Benigni', '1952-10-27', 'Italia');

-- ============================================================
-- Sale
-- ============================================================
INSERT INTO sala (nome, indirizzo, capienza) VALUES ('Sala Grande', 'Via del Cinema 1, Roma', 400);
INSERT INTO sala (nome, indirizzo, capienza) VALUES ('Sala Pasolini', 'Via del Cinema 1, Roma', 180);
INSERT INTO sala (nome, indirizzo, capienza) VALUES ('Cinema Massimo', 'Via Verdi 18, Torino', 250);

-- ============================================================
-- Cast (interpretazioni): relazione molti-a-molti tra Film e Attore,
-- con il personaggio interpretato.
-- ============================================================
INSERT INTO interpretazione (personaggio, film_id, attore_id) VALUES ('Don Vito Corleone', (SELECT id FROM film WHERE titolo = 'Il Padrino'), (SELECT id FROM attore WHERE cognome = 'Brando'));
INSERT INTO interpretazione (personaggio, film_id, attore_id) VALUES ('Michael Corleone', (SELECT id FROM film WHERE titolo = 'Il Padrino'), (SELECT id FROM attore WHERE cognome = 'Pacino'));
INSERT INTO interpretazione (personaggio, film_id, attore_id) VALUES ('Marcello Rubini', (SELECT id FROM film WHERE titolo = 'La Dolce Vita'), (SELECT id FROM attore WHERE cognome = 'Mastroianni'));
INSERT INTO interpretazione (personaggio, film_id, attore_id) VALUES ('Sylvia', (SELECT id FROM film WHERE titolo = 'La Dolce Vita'), (SELECT id FROM attore WHERE cognome = 'Ekberg'));
INSERT INTO interpretazione (personaggio, film_id, attore_id) VALUES ('Alfredo', (SELECT id FROM film WHERE titolo = 'Nuovo Cinema Paradiso'), (SELECT id FROM attore WHERE cognome = 'Noiret'));
INSERT INTO interpretazione (personaggio, film_id, attore_id) VALUES ('Salvatore Di Vita', (SELECT id FROM film WHERE titolo = 'Nuovo Cinema Paradiso'), (SELECT id FROM attore WHERE cognome = 'Troisi'));
INSERT INTO interpretazione (personaggio, film_id, attore_id) VALUES ('Jep Gambardella', (SELECT id FROM film WHERE titolo = 'La grande bellezza'), (SELECT id FROM attore WHERE cognome = 'Servillo'));
INSERT INTO interpretazione (personaggio, film_id, attore_id) VALUES ('Il Biondo', (SELECT id FROM film WHERE titolo = 'Il buono, il brutto, il cattivo'), (SELECT id FROM attore WHERE cognome = 'Eastwood'));
INSERT INTO interpretazione (personaggio, film_id, attore_id) VALUES ('Tuco Ramirez', (SELECT id FROM film WHERE titolo = 'Il buono, il brutto, il cattivo'), (SELECT id FROM attore WHERE cognome = 'Wallach'));
INSERT INTO interpretazione (personaggio, film_id, attore_id) VALUES ('Vincent Vega', (SELECT id FROM film WHERE titolo = 'Pulp Fiction'), (SELECT id FROM attore WHERE cognome = 'Travolta'));
INSERT INTO interpretazione (personaggio, film_id, attore_id) VALUES ('Gerd Wiesler', (SELECT id FROM film WHERE titolo = 'Le vite degli altri'), (SELECT id FROM attore WHERE cognome = 'Ganz'));
INSERT INTO interpretazione (personaggio, film_id, attore_id) VALUES ('Attilio', (SELECT id FROM film WHERE titolo = 'Amarcord'), (SELECT id FROM attore WHERE cognome = 'Benigni'));

-- ============================================================
-- Associazione film-festival (relazione molti-a-molti, lato proprietario Festival)
-- ============================================================LECT
INSERT INTO festival_film (festival_id, film_id) VALUES ((SELECT id FROM festival WHERE nome = 'Festival del Cinema di Roma'), (SELECT id FROM film WHERE titolo = 'Il Padrino'));
INSERT INTO festival_film (festival_id, film_id) VALUES ((SELECT id FROM festival WHERE nome = 'Festival del Cinema di Roma'), (SELECT id FROM film WHERE titolo = 'La Dolce Vita'));
INSERT INTO festival_film (festival_id, film_id) VALUES ((SELECT id FROM festival WHERE nome = 'Festival del Cinema di Roma'), (SELECT id FROM film WHERE titolo = 'Nuovo Cinema Paradiso'));
INSERT INTO festival_film (festival_id, film_id) VALUES ((SELECT id FROM festival WHERE nome = 'Festival del Cinema di Roma'), (SELECT id FROM film WHERE titolo = 'La grande bellezza'));
INSERT INTO festival_film (festival_id, film_id) VALUES ((SELECT id FROM festival WHERE nome = 'Festival del Cinema di Roma'), (SELECT id FROM film WHERE titolo = 'Il buono, il brutto, il cattivo'));
INSERT INTO festival_film (festival_id, film_id) VALUES ((SELECT id FROM festival WHERE nome = 'Festival del Cinema di Roma'), (SELECT id FROM film WHERE titolo = 'Amarcord'));
INSERT INTO festival_film (festival_id, film_id) VALUES ((SELECT id FROM festival WHERE nome = 'Torino Film Festival'), (SELECT id FROM film WHERE titolo = 'Parasite'));
INSERT INTO festival_film (festival_id, film_id) VALUES ((SELECT id FROM festival WHERE nome = 'Torino Film Festival'), (SELECT id FROM film WHERE titolo = 'Interstellar'));
INSERT INTO festival_film (festival_id, film_id) VALUES ((SELECT id FROM festival WHERE nome = 'Torino Film Festival'), (SELECT id FROM film WHERE titolo = 'Le vite degli altri'));
INSERT INTO festival_film (festival_id, film_id) VALUES ((SELECT id FROM festival WHERE nome = 'Torino Film Festival'), (SELECT id FROM film WHERE titolo = 'Pulp Fiction'));
INSERT INTO festival_film (festival_id, film_id) VALUES ((SELECT id FROM festival WHERE nome = 'Torino Film Festival'), (SELECT id FROM film WHERE titolo = 'Il labirinto del fauno'));
INSERT INTO festival_film (festival_id, film_id) VALUES ((SELECT id FROM festival WHERE nome = 'Festival del Cinema Italiano'), (SELECT id FROM film WHERE titolo = 'Le fate ignoranti'));
INSERT INTO festival_film (festival_id, film_id) VALUES ((SELECT id FROM festival WHERE nome = 'Festival del Cinema Italiano'), (SELECT id FROM film WHERE titolo = 'La grande bellezza'));

-- ============================================================
-- Proiezioni
-- Regole rispettate dai dati: il film partecipa al festival della proiezione
-- e nessuna sala risulta occupata due volte nello stesso intervallo.
-- Il Festival del Cinema Italiano (2025) e' concluso: le sue proiezioni sono COMPLETATA.
-- ============================================================
INSERT INTO proiezione (data_ora, stato, festival_id, film_id, sala_id) VALUES ('2026-11-05 20:30:00', 'PROGRAMMATA', (SELECT id FROM festival WHERE nome = 'Festival del Cinema di Roma'), (SELECT id FROM film WHERE titolo = 'Il Padrino'), (SELECT id FROM sala WHERE nome = 'Sala Grande'));
INSERT INTO proiezione (data_ora, stato, festival_id, film_id, sala_id) VALUES ('2026-11-06 18:00:00', 'PROGRAMMATA', (SELECT id FROM festival WHERE nome = 'Festival del Cinema di Roma'), (SELECT id FROM film WHERE titolo = 'La Dolce Vita'), (SELECT id FROM sala WHERE nome = 'Sala Pasolini'));
INSERT INTO proiezione (data_ora, stato, festival_id, film_id, sala_id) VALUES ('2026-11-07 21:00:00', 'PROGRAMMATA', (SELECT id FROM festival WHERE nome = 'Festival del Cinema di Roma'), (SELECT id FROM film WHERE titolo = 'Nuovo Cinema Paradiso'), (SELECT id FROM sala WHERE nome = 'Sala Grande'));
INSERT INTO proiezione (data_ora, stato, festival_id, film_id, sala_id) VALUES ('2026-11-08 19:00:00', 'PROGRAMMATA', (SELECT id FROM festival WHERE nome = 'Festival del Cinema di Roma'), (SELECT id FROM film WHERE titolo = 'La grande bellezza'), (SELECT id FROM sala WHERE nome = 'Sala Pasolini'));
INSERT INTO proiezione (data_ora, stato, festival_id, film_id, sala_id) VALUES ('2026-11-09 20:00:00', 'ANNULLATA', (SELECT id FROM festival WHERE nome = 'Festival del Cinema di Roma'), (SELECT id FROM film WHERE titolo = 'Il buono, il brutto, il cattivo'), (SELECT id FROM sala WHERE nome = 'Sala Grande'));
INSERT INTO proiezione (data_ora, stato, festival_id, film_id, sala_id) VALUES ('2026-11-10 18:30:00', 'PROGRAMMATA', (SELECT id FROM festival WHERE nome = 'Festival del Cinema di Roma'), (SELECT id FROM film WHERE titolo = 'Amarcord'), (SELECT id FROM sala WHERE nome = 'Sala Pasolini'));
INSERT INTO proiezione (data_ora, stato, festival_id, film_id, sala_id) VALUES ('2026-11-20 18:30:00', 'PROGRAMMATA', (SELECT id FROM festival WHERE nome = 'Torino Film Festival'), (SELECT id FROM film WHERE titolo = 'Parasite'), (SELECT id FROM sala WHERE nome = 'Cinema Massimo'));
INSERT INTO proiezione (data_ora, stato, festival_id, film_id, sala_id) VALUES ('2026-11-21 21:00:00', 'PROGRAMMATA', (SELECT id FROM festival WHERE nome = 'Torino Film Festival'), (SELECT id FROM film WHERE titolo = 'Interstellar'), (SELECT id FROM sala WHERE nome = 'Sala Grande'));
INSERT INTO proiezione (data_ora, stato, festival_id, film_id, sala_id) VALUES ('2026-11-22 19:00:00', 'PROGRAMMATA', (SELECT id FROM festival WHERE nome = 'Torino Film Festival'), (SELECT id FROM film WHERE titolo = 'Le vite degli altri'), (SELECT id FROM sala WHERE nome = 'Cinema Massimo'));
INSERT INTO proiezione (data_ora, stato, festival_id, film_id, sala_id) VALUES ('2026-11-23 20:30:00', 'PROGRAMMATA', (SELECT id FROM festival WHERE nome = 'Torino Film Festival'), (SELECT id FROM film WHERE titolo = 'Pulp Fiction'), (SELECT id FROM sala WHERE nome = 'Sala Grande'));
INSERT INTO proiezione (data_ora, stato, festival_id, film_id, sala_id) VALUES ('2026-11-24 18:00:00', 'ANNULLATA', (SELECT id FROM festival WHERE nome = 'Torino Film Festival'), (SELECT id FROM film WHERE titolo = 'Il labirinto del fauno'), (SELECT id FROM sala WHERE nome = 'Sala Pasolini'));
INSERT INTO proiezione (data_ora, stato, festival_id, film_id, sala_id) VALUES ('2025-06-11 21:00:00', 'COMPLETATA', (SELECT id FROM festival WHERE nome = 'Festival del Cinema Italiano'), (SELECT id FROM film WHERE titolo = 'Le fate ignoranti'), (SELECT id FROM sala WHERE nome = 'Cinema Massimo'));
INSERT INTO proiezione (data_ora, stato, festival_id, film_id, sala_id) VALUES ('2025-06-12 20:00:00', 'COMPLETATA', (SELECT id FROM festival WHERE nome = 'Festival del Cinema Italiano'), (SELECT id FROM film WHERE titolo = 'La grande bellezza'), (SELECT id FROM sala WHERE nome = 'Cinema Massimo'));

-- ============================================================
-- Recensioni (una per utente per film: l'utente 2 recensisce film diversi)
-- ============================================================
INSERT INTO recensione (voto, testo, data_creazione, film_id, utente_id) VALUES (5, 'Un affresco familiare che resta il punto piu alto del cinema americano degli anni settanta.', '2026-10-20 21:15:00', (SELECT id FROM film WHERE titolo = 'Il Padrino'), 2);
INSERT INTO recensione (voto, testo, data_creazione, film_id, utente_id) VALUES (5, 'Un film che ha cambiato il modo di raccontare il cinema: la sequenza della fontana e storia.', '2026-10-21 19:40:00', (SELECT id FROM film WHERE titolo = 'La Dolce Vita'), 2);
INSERT INTO recensione (voto, testo, data_creazione, film_id, utente_id) VALUES (4, 'Omaggio sincero alla sala cinematografica e a chi la fa vivere.', '2026-10-22 22:05:00', (SELECT id FROM film WHERE titolo = 'Nuovo Cinema Paradiso'), 2);
INSERT INTO recensione (voto, testo, data_creazione, film_id, utente_id) VALUES (4, 'Ritmo perfetto e una satira sociale che funziona dall inizio alla fine.', '2026-10-23 20:10:00', (SELECT id FROM film WHERE titolo = 'Parasite'), 1);
INSERT INTO recensione (voto, testo, data_creazione, film_id, utente_id) VALUES (3, 'Spettacolare nella forma, un po freddo nel racconto dei personaggi.', '2026-10-24 18:25:00', (SELECT id FROM film WHERE titolo = 'Interstellar'), 1);
