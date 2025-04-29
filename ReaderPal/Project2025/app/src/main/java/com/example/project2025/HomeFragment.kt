package com.example.project2025

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project2025.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val bookViewModel: BookViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookAdapter = BookAdapter(mutableListOf(), { book ->
            Toast.makeText(requireContext(), "Clicked on: ${book.title}", Toast.LENGTH_SHORT).show()
        }, hideButtons = false)
        binding.recyclerViewLinear.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewLinear.adapter = bookAdapter

        bookViewModel.fetchBooks(
            onResult = { books ->
                bookAdapter.updateBooks(books)
                Toast.makeText(requireContext(), "Fetched ${books.size} books", Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply {
            title = "ReaderPal"
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_settings_24)
        }

        (requireActivity() as AppCompatActivity).findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, AddedBooksFragment())
                    .addToBackStack(null)
                    .commit()
            }
            setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, ReaderPalFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }


        //make a search bar on text change to call the searchBooks function
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { query ->
                    bookViewModel.searchBooks(
                        query,
                        onResult = { books ->
                            bookAdapter.updateBooks(books)
                        },
                        onError = { error ->
                            Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                return true
            }
        })
    }
}