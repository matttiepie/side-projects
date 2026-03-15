package springboot.example.demo_api.repo;

import springboot.example.demo_api.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {}