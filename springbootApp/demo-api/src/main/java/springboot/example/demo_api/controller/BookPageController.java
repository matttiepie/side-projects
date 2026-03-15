package springboot.example.demo_api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import springboot.example.demo_api.model.Book;
import springboot.example.demo_api.repo.BookRepository;

@Controller
public class BookPageController {

  private final BookRepository bookRepository;

  public BookPageController(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  // Show the form + existing books
  @GetMapping("/books-page")
  public String booksPage(Model model) {
    model.addAttribute("book", new Book());
    model.addAttribute("books", bookRepository.findAll());
    return "books";
  }

  // Handle HTML form submit
  @PostMapping("/books-page")
  public String createFromForm(@ModelAttribute Book book) {
    book.setId(null);
    bookRepository.save(book);
    return "redirect:/books-page";
  }
}