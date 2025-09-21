package com.example.email_validator.repository;

import com.example.email_validator.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EmailRepository extends JpaRepository<Email, Long> {
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Email e WHERE e.address = :address")
    boolean existsByAddress(@Param("address") String address);

    List<Email> findByStatus(String status);

    List<Email> findByIsActive(Boolean isActive);
}