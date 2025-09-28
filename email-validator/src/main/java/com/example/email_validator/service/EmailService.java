package com.example.email_validator.service;

import com.example.email_validator.model.Email;
import com.example.email_validator.repository.EmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private EmailRepository emailRepository;

    // CREATE
    public Email createEmail(Email email) {
        logger.info("Création d'un nouvel email: {}", email.getAddress());

        validateEmail(email.getAddress());

        if (emailRepository.findByAddress(email.getAddress()).isPresent()) {
            throw new RuntimeException("Cet email existe déjà");
        }

        email.setStatus(determineEmailStatus(email.getAddress()));
        if (email.getIsActive() == null) {
            email.setIsActive(false);
        }

        return emailRepository.save(email);
    }

    // READ
    public List<Email> getAllEmails() {
        return emailRepository.findAll();
    }

    public Email getEmailById(Long id) {
        return emailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email non trouvé: " + id));
    }

    // UPDATE
    public Email updateEmail(Long id, Email emailDetails) {
        Email existingEmail = getEmailById(id);

        if (!existingEmail.getAddress().equals(emailDetails.getAddress())) {
            validateEmail(emailDetails.getAddress());

            if (emailRepository.findByAddress(emailDetails.getAddress()).isPresent()) {
                throw new RuntimeException("Le nouvel email existe déjà");
            }
            existingEmail.setAddress(emailDetails.getAddress());
            existingEmail.setStatus(determineEmailStatus(emailDetails.getAddress()));
        }

        existingEmail.setStatus(emailDetails.getStatus());
        existingEmail.setIsActive(emailDetails.getIsActive());

        return emailRepository.save(existingEmail);
    }

    // DELETE
    public void deleteEmail(Long id) {
        if (!emailRepository.existsById(id)) {
            throw new RuntimeException("Email non trouvé: " + id);
        }
        emailRepository.deleteById(id);
    }

    public void deleteAllEmails() {
        emailRepository.deleteAll();
    }

    // SEARCH
    public List<Email> searchEmails(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllEmails();
        }

        String term = searchTerm.toLowerCase().trim();
        Set<Email> results = new HashSet<>();

        results.addAll(emailRepository.findByAddressContainingIgnoreCase(term));
        results.addAll(emailRepository.findByStatusContainingIgnoreCase(term));

        return new ArrayList<>(results);
    }

    // CSV PROCESSING
    // Dans EmailService.java - méthode processCSVFile
    public Map<String, Object> processCSVFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }

        // AJOUT: Vérification taille fichier
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB max
            throw new RuntimeException("Fichier trop volumineux (max 10MB)");
        }

        Map<String, Object> result = new HashMap<>();
        List<Email> savedEmails = new ArrayList<>();

        int totalLines = 0, validCount = 0, obsoleteCount = 0, invalidCount = 0, duplicateCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) { // AJOUT: charset
            String line;
            Set<String> processedEmails = new HashSet<>(); // Pour éviter doublons en mémoire

            while ((line = reader.readLine()) != null) {
                totalLines++;
                String emailAddress = line.trim();

                // AJOUT: Log pour debug
                if (totalLines % 100 == 0) {
                    logger.info("Traitement ligne {}: {}", totalLines, emailAddress);
                }

                if (emailAddress.isEmpty() || (totalLines == 1 && isCSVHeader(emailAddress))) {
                    continue;
                }

                // AJOUT: Limiter la longueur de l'email
                if (emailAddress.length() > 254) {
                    invalidCount++;
                    continue;
                }

                if (!isValidEmail(emailAddress)) {
                    invalidCount++;
                    continue;
                }

                String status = determineEmailStatus(emailAddress);
                if ("valid".equals(status)) validCount++;
                else if ("obsolete".equals(status)) obsoleteCount++;
                else invalidCount++;

                // AJOUT: Vérifier doublons dans le fichier actuel
                if (!processedEmails.add(emailAddress.toLowerCase())) {
                    duplicateCount++;
                    continue;
                }

                if (emailRepository.findByAddress(emailAddress).isPresent()) {
                    duplicateCount++;
                    continue;
                }

                Email email = new Email(emailAddress, status, false);
                Email savedEmail = emailRepository.save(email);
                savedEmails.add(savedEmail);

                // AJOUT: Sauvegarde par lots pour grandes quantités
                if (savedEmails.size() % 50 == 0) {
                    emailRepository.flush(); // Force la sauvegarde
                }
            }

            // AJOUT: Log final
            logger.info("Traitement terminé: {} lignes, {} emails sauvegardés", totalLines, savedEmails.size());

            result.put("success", true);
            result.put("message", "Fichier CSV traité avec succès");
            result.put("totalLines", totalLines);
            result.put("validEmails", validCount);
            result.put("obsoleteEmails", obsoleteCount);
            result.put("invalidEmails", invalidCount);
            result.put("duplicates", duplicateCount);
            result.put("savedEmails", savedEmails.size());
            result.put("emails", savedEmails);

        } catch (Exception e) {
            logger.error("Erreur traitement CSV à la ligne {}", totalLines, e);
            throw new RuntimeException("Erreur ligne " + totalLines + ": " + e.getMessage());
        }

        return result;
    }

    // STATISTICS
    public Map<String, Object> getEmailStats() {
        Map<String, Object> stats = new HashMap<>();

        long total = emailRepository.count();
        long valid = emailRepository.countByStatus("valid");
        long obsolete = emailRepository.countByStatus("obsolete");
        long invalid = emailRepository.countByStatus("invalid");
        long active = emailRepository.countByIsActive(true);

        stats.put("totalEmails", total);
        stats.put("validEmails", valid);
        stats.put("obsoleteEmails", obsolete);
        stats.put("invalidEmails", invalid);
        stats.put("activeEmails", active);

        return stats;
    }

    public List<Email> getValidEmails() {
        return emailRepository.findValidEmails();
    }

    // VALIDATION METHODS
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("L'adresse email est requise");
        }

        if (!isValidEmail(email)) {
            throw new RuntimeException("Format d'email invalide: " + email);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private String determineEmailStatus(String email) {
        if (email.contains("edu") || email.contains("ac.")) return "valid";
        if (email.contains("hotmail") || email.contains("yahoo")) return "obsolete";
        return "invalid";
    }

    private boolean isCSVHeader(String line) {
        if (line == null) return false;
        String lowerLine = line.toLowerCase().trim();
        return lowerLine.equals("email") || lowerLine.equals("address");
    }
}