package org.gnori.booksmarket.storage.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.storage.entity.enums.Language;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
@Table(name = "book")
public class BookEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  Long id;

  String name;

  String description;

  @ManyToMany (fetch = FetchType.EAGER,
      cascade = {CascadeType.MERGE, CascadeType.DETACH})
  @JoinTable (
      name = "book_author",
      joinColumns = {@JoinColumn (name = "book_id")},
      inverseJoinColumns = {@JoinColumn(name = "author_id")})
  List<AuthorEntity> authors = new ArrayList<>();

  @ManyToMany (fetch = FetchType.EAGER,
      cascade = {CascadeType.MERGE, CascadeType.DETACH})
  @JoinTable (
      name = "book_genre",
      joinColumns = {@JoinColumn (name = "book_id")},
      inverseJoinColumns = {@JoinColumn(name = "genre_id")})
  List<GenreEntity> genres = new ArrayList<>();
  // YYYY-mm-DD
  @Column(name = "release_date")
  Date releaseDate;

  @Enumerated(value = EnumType.ORDINAL)
  Language language;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "book_id")
  List<ReviewEntity> reviews = new ArrayList<>();

  @ManyToOne(fetch = FetchType.EAGER,
      cascade = {CascadeType.MERGE, CascadeType.DETACH})
  @JoinColumn(name = "publisher_id")
  PublisherEntity publisher;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BookEntity bookEntity = (BookEntity) o;
    return getId().equals(bookEntity.getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }
}
