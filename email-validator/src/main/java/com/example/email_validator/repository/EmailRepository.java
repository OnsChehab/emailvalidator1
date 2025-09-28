package com.example.email_validator.repository;

import com.example.email_validator.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
    Optional<Email> findByAddress(String address);
    List<Email> findByStatus(String status);
    List<Email> findByIsActive(Boolean isActive);
    boolean existsByAddress(String address);

    // CORRECTION : Retirer OrderBy si le champ n'existe pas
    List<Email> findAll();

    @Query("SELECT e FROM Email e WHERE LOWER(e.address) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Email> findByAddressContainingIgnoreCase(@Param("term") String term);

    @Query("SELECT e FROM Email e WHERE LOWER(e.status) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Email> findByStatusContainingIgnoreCase(@Param("term") String term);

    long countByStatus(String status);
    long countByIsActive(Boolean isActive);

    @Query("SELECT e FROM Email e WHERE e.status = 'valid'")
    List<Email> findValidEmails();

    @Query("SELECT e FROM Email e WHERE e.status = 'invalid'")
    List<Email> findInvalidEmails();

    @Query("SELECT e FROM Email e WHERE e.status = 'obsolete'")
    List<Email> findObsoleteEmails();
}