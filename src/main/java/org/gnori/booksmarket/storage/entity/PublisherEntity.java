package org.gnori.booksmarket.storage.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
@Table(name = "publisher")
public class PublisherEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  Long id;

  String name;
  @ManyToMany(fetch = FetchType.LAZY,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH})
  List<AuthorEntity> authors = new ArrayList<>();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PublisherEntity publisherEntity = (PublisherEntity) o;
    return getId().equals(publisherEntity.getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }
}
