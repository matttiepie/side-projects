package com.example.project2025

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object BookRepository {
    private val addedBooks: MutableList<BookAPI.Book> = mutableListOf()

    fun addBook(book: BookAPI.Book) {
        if (!addedBooks.contains(book)) {
            addedBooks.add(book)
        }
    }

    fun getAddedBooks(): List<BookAPI.Book> {
        return addedBooks
    }

    fun removeBook(book: BookAPI.Book) {
        addedBooks.remove(book)
    }

    fun removeBookFromFirebase(book: BookAPI.Book) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("BookRepository", "User not signed in")
            return
        }

        val userBooksCollection = FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .collection("books")

        userBooksCollection.whereEqualTo("title", book.title)
            .whereEqualTo("author", book.author)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    userBooksCollection.document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("BookRepository", "Book deleted successfully: ${book.title}")
                        }
                        .addOnFailureListener { e ->
                            Log.e("BookRepository", "Error deleting book: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("BookRepository", "Error finding book: ${e.message}")
            }
    }

    fun removeAllBooks() {
        addedBooks.clear()
    }
}