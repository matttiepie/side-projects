package com.example.project2025

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project2025.databinding.ItemBookBinding


class BookAdapter(
    private val books: MutableList<BookAPI.Book>,
    private val onBookClicked: (BookAPI.Book) -> Unit,
    private val hideButtons: Boolean = false,
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(
        private val binding: ItemBookBinding,
        private val hideButtons: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: BookAPI.Book, onBookClicked: (BookAPI.Book) -> Unit) {
            binding.textViewBookTitle.text = book.title
            binding.textViewBookAuthor.text = book.author
            binding.imageButtonAdd.setOnClickListener {
                onBookClicked(book)
                val bookViewModel: BookViewModel = BookViewModel()
                bookViewModel.addBooks(book)
            }
            if (!hideButtons) {
                binding.imageButtonDelete.setOnClickListener {
                    onBookClicked(book)
                    val bookViewModel: BookViewModel = BookViewModel()
                    bookViewModel.removeBook(book)
                }
            } else {
                binding.imageButtonDelete.setOnClickListener {
                    onBookClicked(book)
                    val bookViewModel: BookViewModel = BookViewModel()
                    bookViewModel.removeBookFromFirebase(book)
                }
            }

            binding.imageButtonAdd.visibility = if (hideButtons) View.GONE else View.VISIBLE
//            binding.imageButtonDelete.visibility = if (hideButtons) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding, hideButtons)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position], onBookClicked)
    }

    override fun getItemCount(): Int = books.size

    fun updateBooks(newBooks: Collection<BookAPI.Book>) {
        books.clear()
        books.addAll(newBooks)
        notifyDataSetChanged()
    }
    fun removeBook(book: BookAPI.Book) {
        books.remove(book)
        notifyDataSetChanged()
    }
}
