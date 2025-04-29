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
import com.example.project2025.databinding.ReaderPalFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReaderPalFragment : Fragment() {
    private lateinit var binding: ReaderPalFragmentBinding
    private val bookViewModel: BookViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ReaderPalFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .addToBackStack(null)
                .commit()
        }

        // Fetch the books and create a new list instance for the adapter
        fetchBooksFromFirebase { books ->
            bookAdapter = BookAdapter(books.toMutableList(), { book ->
                Toast.makeText(requireContext(), "Clicked on: ${book.title}", Toast.LENGTH_SHORT).show()
                bookAdapter.removeBook(book)
            }, hideButtons = true)
            binding.readerPalRV.layoutManager = LinearLayoutManager(requireContext())
            binding.readerPalRV.adapter = bookAdapter
        }




        // Upload the added books to Firebase
//        uploadAddedBooksToFirebase(books)
    }

    private fun fetchBooksFromFirebase(onBooksFetched: (List<BookAPI.Book>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        val userBooksCollection = db.collection("users").document(currentUser.uid).collection("books")

        userBooksCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val books = querySnapshot.documents.mapNotNull { document ->
                    val title = document.getString("title")
                    val author = document.getString("author")
                    if (title != null && author != null) {
                        BookAPI.Book(title, author)
                    } else {
                        null
                    }
                }
                onBooksFetched(books)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching books: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}