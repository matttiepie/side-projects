package springboot.example.demo_api.controller;

import org.springframework.web.bind.annotation.*;
import springboot.example.demo_api.model.Book;
import springboot.example.demo_api.repo.BookRepository;
import java.util.List;

@RestController // This tells Spring to return JSON, not HTML views
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:5173") // Allows React to talk to this API
public class BookRestController {

    private final BookRepository bookRepository;

    public BookRestController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // Get all books as JSON
    @GetMapping
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Save a book sent as JSON from React
    @PostMapping
    public Book createBook(@RequestBody Book book) {
        return bookRepository.save(book);
    }
}