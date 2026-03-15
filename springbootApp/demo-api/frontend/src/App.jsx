import { useEffect, useState } from 'react';

function App() {
  const [books, setBooks] = useState([]);
  const [title, setTitle] = useState('');

  // 1. Fetch books from the backend
  const fetchBooks = async () => {
    const response = await fetch('http://localhost:8080/api/books');
    const data = await response.json();
    setBooks(data);
  };

  useEffect(() => {
    fetchBooks();
  }, []);

  // 2. Post a new book
  const handleSubmit = async (e) => {
    e.preventDefault();
    await fetch('http://localhost:8080/api/books', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title: title }) // Match your Book model fields
    });
    setTitle('');
    fetchBooks(); // Refresh the list
  };

  return (
      <div>
        <h1>Book List</h1>
        <form onSubmit={handleSubmit}>
          <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Book Title" />
          <button type="submit">Add Book</button>
        </form>

        <ul>
          {books.map(book => (
              <li key={book.id}>{book.title}</li>
          ))}
        </ul>
      </div>
  );
}

export default App;