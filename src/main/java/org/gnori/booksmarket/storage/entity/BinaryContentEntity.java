package org.gnori.booksmarket.storage.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Table(name = "binary_content")
public class BinaryContentEntity {

  @Id
  Long id;

  byte[] raw;

  @Column(name = "type_raw")
  String typeRaw;

  @Column(name="size_raw")
  Double sizeRaw;

  byte[] image;

  @Column(name = "type_image")
  String typeImage;

  @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.DETACH})
  BookEntity book;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BinaryContentEntity that = (BinaryContentEntity) o;

    return getId().equals(that.getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }
}
