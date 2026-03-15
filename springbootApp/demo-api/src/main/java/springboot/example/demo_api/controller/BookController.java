package springboot.example.demo_api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import springboot.example.demo_api.model.Book;
import springboot.example.demo_api.repo.BookRepository;

@RestController
@RequestMapping("/books")
public class BookController {

  private final BookRepository bookRepository;

  public BookController(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  @GetMapping
  public List<Book> list() {
    return bookRepository.findAll();
  }

  @PostMapping
  public Book create(@RequestBody Book book) {
    // ignore client-provided id
    book.setId(null);
    return bookRepository.save(book);
  }
}