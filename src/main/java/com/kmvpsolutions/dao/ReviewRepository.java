package com.kmvpsolutions.dao;

import com.kmvpsolutions.domain.Customer;
import com.kmvpsolutions.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("select p.reviews From Product p where p.id = ?1")
    List<Review> findReviewsByProductId(Long id);
}
