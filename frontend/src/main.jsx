import { createRoot } from 'react-dom/client';
import FilmList from './FilmList.jsx';

const root = document.getElementById('film-list-root');
if (root) {
  createRoot(root).render(<FilmList />);
}
