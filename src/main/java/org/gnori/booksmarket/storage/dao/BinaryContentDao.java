package org.gnori.booksmarket.storage.dao;

import org.gnori.booksmarket.storage.entity.BinaryContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BinaryContentDao extends JpaRepository<BinaryContentEntity, Long> {

}
