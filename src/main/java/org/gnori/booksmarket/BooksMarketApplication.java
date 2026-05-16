package org.gnori.booksmarket;

import org.gnori.booksmarket.feauture.attachments.AttachmentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableConfigurationProperties({ AttachmentProperties.class })
public class BooksMarketApplication {

  public static void main(String[] args) {
    SpringApplication.run(BooksMarketApplication.class, args);
  }

}
