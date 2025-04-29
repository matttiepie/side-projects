package com.example.project2025

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project2025.databinding.FragmentAddedBooksBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddedBooksFragment : Fragment() {

    private lateinit var binding: FragmentAddedBooksBinding
    private val bookViewModel: BookViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddedBooksBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the toolbar title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Added Books"

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .addToBackStack(null)
                .commit()
        }

        // Fetch the books and create a new list instance for the adapter
        val books = bookViewModel.fetchAddedBooks().toList() // Create a copy of the list
        bookAdapter = BookAdapter(books.toMutableList(), { book: BookAPI.Book ->
            bookViewModel.removeBook(book) // Remove the book from the ViewModel or database
            bookAdapter.removeBook(book)  // Remove the book from the adapter
            Toast.makeText(requireContext(), "Deleted: ${book.title}", Toast.LENGTH_SHORT).show()
        }, hideButtons = false)

        binding.addedBookRV.layoutManager = LinearLayoutManager(requireContext())
        binding.addedBookRV.adapter = bookAdapter
        binding.addedBookRV.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        // Upload the added books to Firebase
        binding.addBookstoReaderPal.setOnClickListener {
            uploadAddedBooksToFirebase(books)
            Toast.makeText(requireContext(), "Books uploaded to ReaderPal", Toast.LENGTH_SHORT).show()
            bookViewModel.removeAllBooks()
            bookAdapter.updateBooks(emptyList()) // Clear the adapter's data
            bookAdapter.notifyDataSetChanged() // Notify the adapter to refresh the list
        }
    }

    private fun uploadAddedBooksToFirebase(addedBooks: List<BookAPI.Book>) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        val userBooksCollection = db.collection("users").document(currentUser.uid).collection("books")

        addedBooks.forEach { book ->
            userBooksCollection
                .whereEqualTo("title", book.title)
                .whereEqualTo("author", book.author)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        val bookData = mapOf(
                            "title" to book.title,
                            "author" to book.author,
                        )
                        userBooksCollection.add(bookData)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Book uploaded: ${book.title}", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Error uploading book: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(requireContext(), "Book already exists: ${book.title}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error checking for duplicates: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


}