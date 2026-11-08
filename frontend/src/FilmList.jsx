import { useEffect, useMemo, useState } from 'react';
import axios from 'axios';

const API = '/api/film';

function etichettaGenere(genere) {
  if (!genere) {
    return '';
  }
  return genere.charAt(0) + genere.slice(1).toLowerCase();
}

export default function FilmList() {
  const [genere, setGenere] = useState('');
  const [film, setFilm] = useState([]);
  const [generi, setGeneri] = useState([]);
  const [caricamento, setCaricamento] = useState(true);
  const [errore, setErrore] = useState(null);

  useEffect(() => {
    axios.get(`${API}/generi`)
      .then((risposta) => setGeneri(risposta.data))
      .catch(() => setErrore('Impossibile caricare i generi.'));
  }, []);

  // il catalogo si carica una sola volta: il filtro per genere viene applicato
  // nel browser, senza una richiesta a ogni selezione
  useEffect(() => {
    axios.get(API)
      .then((risposta) => setFilm(risposta.data))
      .catch(() => setErrore('Impossibile caricare i film.'))
      .finally(() => setCaricamento(false));
  }, []);

  // il filtraggio viene ricalcolato solo quando cambiano il catalogo o il genere
  // scelto; se il componente si ridisegna per altri motivi, il risultato viene riusato
  const filmFiltrati = useMemo(() => {
    return film.filter((f) => !genere || f.genere === genere);
  }, [film, genere]);

  const conteggio = filmFiltrati.length === 1 ? '1 film trovato' : `${filmFiltrati.length} film trovati`;

  return (
    <>
      <div className="filtri-film">
        <div className="form-group">
          <label htmlFor="filtro-genere">Genere</label>
          <select
            id="filtro-genere"
            value={genere}
            onChange={(evento) => setGenere(evento.target.value)}
          >
            <option value="">Tutti i generi</option>
            {generi.map((valore) => (
              <option key={valore} value={valore}>
                {etichettaGenere(valore)}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="film-widget-stato" aria-live="polite">
        {caricamento && 'Caricamento film...'}
        {!caricamento && !errore && conteggio}
      </div>

      {errore && <div className="error-msg">{errore}</div>}

      {!caricamento && !errore && filmFiltrati.length === 0 && (
        <p>Nessun film trovato con il genere selezionato.</p>
      )}

      {!errore && filmFiltrati.length > 0 && (
        <div className="griglia-card">
          {filmFiltrati.map((f) => (
            <div className="card" key={f.id}>
              <h2>{f.titolo}</h2>
              <ul className="lista-info">
                <li>
                  <strong>Anno:</strong> {f.anno}
                </li>
                <li>
                  <strong>Regista:</strong> {f.regista}
                </li>
                <li>
                  <strong>Genere:</strong> {etichettaGenere(f.genere)}
                </li>
                <li>
                  <strong>Durata:</strong> {f.durata} min
                </li>
              </ul>
              <a className="btn" href={`/film/${f.id}`}>
                Dettaglio
              </a>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
