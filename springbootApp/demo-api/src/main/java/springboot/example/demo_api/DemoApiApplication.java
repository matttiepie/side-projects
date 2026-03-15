package springboot.example.demo_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class DemoApiApplication {
  public static void main(String[] args) {
    System.out.println("MAIN CLASS = " + DemoApiApplication.class.getName());
    SpringApplication.run(DemoApiApplication.class, args);
  }
}
