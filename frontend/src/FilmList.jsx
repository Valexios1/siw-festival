import { useEffect, useState } from 'react';

const API = '/api/film';

function etichettaGenere(genere) {
  if (!genere) {
    return '';
  }
  return genere.charAt(0) + genere.slice(1).toLowerCase();
}

export default function FilmList() {
  const [titolo, setTitolo] = useState('');
  const [genere, setGenere] = useState('');
  const [film, setFilm] = useState([]);
  const [generi, setGeneri] = useState([]);
  const [caricamento, setCaricamento] = useState(true);
  const [errore, setErrore] = useState(null);

  useEffect(() => {
    const controller = new AbortController();

    fetch(`${API}/generi`, { signal: controller.signal })
      .then((risposta) => {
        if (!risposta.ok) {
          throw new Error('Impossibile caricare i generi.');
        }
        return risposta.json();
      })
      .then(setGeneri)
      .catch((e) => {
        if (e.name !== 'AbortError') {
          setErrore(e.message);
        }
      });

    return () => controller.abort();
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    const timer = setTimeout(() => {
      const parametri = new URLSearchParams();
      if (titolo.trim()) {
        parametri.set('titolo', titolo.trim());
      }
      if (genere) {
        parametri.set('genere', genere);
      }

      const query = parametri.toString();
      setCaricamento(true);
      setErrore(null);

      fetch(query ? `${API}?${query}` : API, { signal: controller.signal })
        .then((risposta) => {
          if (!risposta.ok) {
            throw new Error('Impossibile caricare i film.');
          }
          return risposta.json();
        })
        .then((dati) => {
          setFilm(dati);
          setCaricamento(false);
        })
        .catch((e) => {
          if (e.name === 'AbortError') {
            return;
          }
          setErrore(e.message);
          setCaricamento(false);
        });
    }, 250);

    return () => {
      clearTimeout(timer);
      controller.abort();
    };
  }, [titolo, genere]);

  const conteggio = film.length === 1 ? '1 film trovato' : `${film.length} film trovati`;

  return (
    <>
      <div className="filtri-film">
        <div className="form-group">
          <label htmlFor="filtro-titolo">Cerca per titolo</label>
          <input
            id="filtro-titolo"
            type="search"
            value={titolo}
            onChange={(evento) => setTitolo(evento.target.value)}
            placeholder="Es. Il Padrino"
            autoComplete="off"
          />
        </div>

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

        <button
          type="button"
          className="btn btn-azzera"
          onClick={() => {
            setTitolo('');
            setGenere('');
          }}
          disabled={!titolo && !genere}
        >
          Azzera filtri
        </button>
      </div>

      <div className="film-widget-stato" aria-live="polite">
        {caricamento && 'Caricamento film...'}
        {!caricamento && !errore && conteggio}
      </div>

      {errore && <div className="error-msg">{errore}</div>}

      {!caricamento && !errore && film.length === 0 && (
        <p>Nessun film trovato con i filtri selezionati.</p>
      )}

      {!errore && film.length > 0 && (
        <div className="griglia-card">
          {film.map((f) => (
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
