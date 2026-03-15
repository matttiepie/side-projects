package springboot.example.demo_api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private boolean completed = false; // optional: rename this too (see note below)

  public Book() {}

  public Book(String title) {
    this.title = title;
  }

  public Long getId() { return id; }
  public String getTitle() { return title; }
  public boolean isCompleted() { return completed; }

  public void setId(Long id) { this.id = id; }
  public void setTitle(String title) { this.title = title; }
  public void setCompleted(boolean completed) { this.completed = completed; }
}