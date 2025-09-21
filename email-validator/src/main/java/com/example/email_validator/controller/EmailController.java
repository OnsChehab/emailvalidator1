package com.example.email_validator.controller;

import com.example.email_validator.model.Email;
import com.example.email_validator.repository.EmailRepository;
import com.example.email_validator.service.EmailValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@RestController
@RequestMapping("/api")
public class EmailController {
    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private EmailValidationService emailValidationService;

    @GetMapping("/emails")
    public List<Email> getAllEmails() {
        logger.info("Fetching all emails");
        List<Email> emails = emailRepository.findAll();
        logger.info("Found {} emails", emails.size());
        return emails;
    }

    @PostMapping("/email")
    public ResponseEntity<?> addEmail(@RequestBody Email email) {
        try {
            logger.info("Adding email: {}", email.getAddress());

            // Vérifier si l'email existe déjà
            if (emailRepository.existsByAddress(email.getAddress())) {
                return ResponseEntity.badRequest().body("Cet email existe déjà dans la base de données");
            }

            // Validation de l'email
            String status = emailValidationService.validateEmail(email.getAddress());
            email.setStatus(status);
            email.setIsActive("valide".equals(status));

            Email savedEmail = emailRepository.save(email);
            return ResponseEntity.ok(savedEmail);

        } catch (Exception e) {
            logger.error("Error adding email: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error adding email: " + e.getMessage());
        }
    }

    @PutMapping("/emails")
    public ResponseEntity<?> updateEmail(@RequestBody Email email) {
        try {
            logger.info("Updating email: {}", email.getAddress());

            if (email.getId() == null) {
                return ResponseEntity.badRequest().body("Email ID is required for update");
            }

            Optional<Email> existingEmail = emailRepository.findById(email.getId());
            if (existingEmail.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Re-validation après modification
            String status = emailValidationService.validateEmail(email.getAddress());
            email.setStatus(status);
            email.setIsActive("valide".equals(status));

            Email updatedEmail = emailRepository.save(email);
            return ResponseEntity.ok(updatedEmail);

        } catch (Exception e) {
            logger.error("Error updating email: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error updating email: " + e.getMessage());
        }
    }

    @DeleteMapping("/emails/{id}")
    public ResponseEntity<String> deleteEmail(@PathVariable Long id) {
        try {
            logger.info("Deleting email with ID: {}", id);

            if (!emailRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            emailRepository.deleteById(id);
            return ResponseEntity.ok("Email deleted successfully");

        } catch (Exception e) {
            logger.error("Error deleting email: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error deleting email: " + e.getMessage());
        }
    }

    @PostMapping("/emails/upload-csv")
    public ResponseEntity<?> uploadCSV(@RequestParam("file") MultipartFile file) {
        logger.info("Received CSV file: {}", file.getOriginalFilename());
        List<Email> emails = new ArrayList<>();
        Set<String> uniqueEmails = new HashSet<>(); // Pour détecter les doublons dans le fichier
        int duplicateCount = 0;
        int invalidCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }

                String[] data = line.split(",");
                if (data.length > 0 && !data[0].trim().isEmpty()) {
                    String emailAddress = data[0].trim().toLowerCase();

                    // Vérifier les doublons dans le fichier
                    if (uniqueEmails.contains(emailAddress)) {
                        duplicateCount++;
                        continue;
                    }
                    uniqueEmails.add(emailAddress);

                    // Vérifier si l'email existe déjà en base
                    if (emailRepository.existsByAddress(emailAddress)) {
                        duplicateCount++;
                        continue;
                    }

                    // Validation de l'email
                    String status = emailValidationService.validateEmail(emailAddress);

                    if ("erronée".equals(status)) {
                        invalidCount++;
                        continue;
                    }

                    Email email = new Email();
                    email.setAddress(emailAddress);
                    email.setStatus(status);
                    email.setIsActive("valide".equals(status));

                    emails.add(emailRepository.save(email));
                }
            }

            logger.info("Uploaded {} emails from CSV, {} doublons ignorés, {} invalides ignorés",
                    emails.size(), duplicateCount, invalidCount);

            return ResponseEntity.ok(emails);

        } catch (Exception e) {
            logger.error("Error processing CSV: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to process CSV file: " + e.getMessage());
        }
    }

    @PostMapping("/emails/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody Email emailRequest) {
        try {
            logger.info("Verifying email: {}", emailRequest.getAddress());

            if (emailRequest.getId() == null) {
                return ResponseEntity.badRequest().body("Email ID is required for verification");
            }

            Optional<Email> existingEmail = emailRepository.findById(emailRequest.getId());
            if (existingEmail.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Email emailToVerify = existingEmail.get();

            // Re-vérification de l'email
            String status = emailValidationService.validateEmail(emailToVerify.getAddress());
            emailToVerify.setStatus(status);
            emailToVerify.setIsActive("valide".equals(status));
            emailRepository.save(emailToVerify);

            return ResponseEntity.ok("Email " + emailToVerify.getAddress() + " est " + status);

        } catch (Exception e) {
            logger.error("Error verifying email: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error verifying email: " + e.getMessage());
        }
    }

    @PostMapping("/emails/verify-batch")
    public ResponseEntity<?> verifyBatchEmails() {
        try {
            logger.info("Starting batch verification");

            List<Email> emails = emailRepository.findAll();
            List<String> results = new ArrayList<>();
            int verifiedCount = 0;

            for (Email email : emails) {
                try {
                    // Re-vérification de l'email
                    String status = emailValidationService.validateEmail(email.getAddress());
                    email.setStatus(status);
                    email.setIsActive("valide".equals(status));
                    emailRepository.save(email);

                    results.add(email.getAddress() + ": " + status);
                    verifiedCount++;

                    logger.info("Verified {}/{}: {}", verifiedCount, emails.size(), email.getAddress());

                } catch (Exception e) {
                    logger.error("Error verifying email {}: {}", email.getAddress(), e.getMessage());
                    results.add(email.getAddress() + ": ERROR - " + e.getMessage());
                }
            }

            String message = "Batch verification completed. " + verifiedCount + " emails verified.";
            logger.info(message);

            return ResponseEntity.ok(message);

        } catch (Exception e) {
            logger.error("Error in batch verification: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error in batch verification: " + e.getMessage());
        }
    }

    @GetMapping("/emails/stats")
    public ResponseEntity<?> getEmailStats() {
        try {
            logger.info("Calculating email statistics");

            List<Email> emails = emailRepository.findAll();
            long total = emails.size();
            long valide = emails.stream().filter(e -> "valide".equals(e.getStatus())).count();
            long erronee = emails.stream().filter(e -> "erronée".equals(e.getStatus())).count();
            long obsolete = emails.stream().filter(e -> "obsolète".equals(e.getStatus())).count();

            String stats = String.format(
                    "Total: %d, Valide: %d, Erronée: %d, Obsolète: %d",
                    total, valide, erronee, obsolete
            );

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("Error calculating statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error calculating statistics: " + e.getMessage());
        }
    }
    @GetMapping("/emails/export-valid")
    public ResponseEntity<?> exportValidEmails() {
        try {
            logger.info("Exporting valid emails");

            // Récupérer tous les emails valides
            List<Email> validEmails = emailRepository.findAll().stream()
                    .filter(e -> "valide".equals(e.getStatus()))
                    .toList();

            // Éliminer les doublons
            Set<String> uniqueEmails = new HashSet<>();
            for (Email email : validEmails) {
                uniqueEmails.add(email.getAddress().toLowerCase());
            }

            // Créer le contenu CSV
            StringBuilder csvContent = new StringBuilder("email\n");
            for (String email : uniqueEmails) {
                csvContent.append(email).append("\n");
            }

            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv; charset=utf-8")
                    .header("Content-Disposition", "attachment; filename=emails_valides_uniques.csv")
                    .body(csvContent.toString());

        } catch (Exception e) {
            logger.error("Error exporting emails: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error exporting emails: " + e.getMessage());
        }
    }
}