package org.gnori.booksmarket.dao;

import org.gnori.booksmarket.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewDao extends JpaRepository<ReviewEntity, Long> {

}
