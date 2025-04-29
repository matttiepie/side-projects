package com.example.project2025

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BookViewModel : ViewModel() {

    private val bookService = BookAPI().service
    var addedBooks: MutableList<BookAPI.Book> = mutableListOf()

    fun fetchBooks(onResult: (List<BookAPI.Book>) -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            try {
                val response = bookService.getBooks()
                val books = response.docs.map { doc ->
                    BookAPI.Book(
                        title = doc.title,
                        author = doc.author_name?.joinToString(", ") ?: "Unknown"
                    )
                }
                onResult(books)
            } catch (e: Exception) {
                onError(e)
                Log.e("BookViewModel", "Error fetching books", e)
            }
        }
    }

    fun searchBooks(query: String, onResult: (List<BookAPI.Book>) -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            try {
                val response = bookService.getSearchBooks(query)
                val books = response.docs.map { doc ->
                    BookAPI.Book(
                        title = doc.title,
                        author = doc.author_name?.joinToString(", ") ?: "Unknown"
                    )
                }
                onResult(books)
            } catch (e: Exception) {
                onError(e)
                Log.e("BookViewModel", "Error fetching books", e)
            }
        }
    }

    fun fetchAddedBooks(): List<BookAPI.Book> {
        Log.d("BookViewModel", "Fetching added books from repository")
        return BookRepository.getAddedBooks()
    }

    fun addBooks(book: BookAPI.Book) {
        BookRepository.addBook(book)
        Log.d("BookViewModel", "Added book to repository: $book")
    }

    fun removeBook(book: BookAPI.Book) {
        BookRepository.removeBook(book)
        Log.d("BookViewModel", "Removed book from repository: $book")
    }
    //make function that removes book from firebase
    fun removeBookFromFirebase(book: BookAPI.Book) {
        BookRepository.removeBookFromFirebase(book)
        Log.d("BookViewModel", "Removed book from Firebase: $book")
    }

    fun removeAllBooks() {
        // Clear the list of added books
        addedBooks.clear()
        BookRepository.removeAllBooks()
    }
}