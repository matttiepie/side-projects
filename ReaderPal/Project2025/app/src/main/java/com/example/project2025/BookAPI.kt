package com.example.project2025

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class BookAPI {

    private val url = "https://openlibrary.org/" // Replace with your API base URL

    // Define the Retrofit service interface
    interface BookService {
        @GET("search.json?q=the+lord+of+the+rings&limit=20")
        suspend fun getBooks(): BookResponse

        @GET("search.json?limit=20")
        suspend fun getSearchBooks(@retrofit2.http.Query("q") query: String): BookResponse
    }
    // Data models for the API response


    data class Book(
        val title: String,
        val author: String
    )

    data class BookResponse(
        val numFound: Int,
        val start: Int,
        val numFoundExact: Boolean,
        val docs: List<BookDoc>
    )

    data class BookDoc(
        val title: String,
        val author_name: List<String>?,
        val key: String,
        val editions: EditionResponse?
    )

    data class EditionResponse(
        val numFound: Int,
        val start: Int,
        val numFoundExact: Boolean,
        val docs: List<EditionDoc>
    )

    data class EditionDoc(
        val key: String,
        val title: String
    )

    private val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: BookService = retrofit.create(BookService::class.java)
}