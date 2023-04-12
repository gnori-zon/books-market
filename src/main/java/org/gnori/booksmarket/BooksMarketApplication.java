package org.gnori.booksmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class BooksMarketApplication {

  public static void main(String[] args) {
    SpringApplication.run(BooksMarketApplication.class, args);
  }

}
