package com.example.email_validator.controller;

import com.example.email_validator.model.Email;
import com.example.email_validator.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emails")
@Tag(name = "Emails Management", description = "API de gestion des emails")
@CrossOrigin(origins = "*")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/upload-csv")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Uploader un fichier CSV")
    public ResponseEntity<?> uploadCSV(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = emailService.processCSVFile(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lister tous les emails")
    public ResponseEntity<List<Email>> getAllEmails() {
        return ResponseEntity.ok(emailService.getAllEmails());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Récupérer un email par ID")
    public ResponseEntity<?> getEmailById(@PathVariable Long id) {
        try {
            Email email = emailService.getEmailById(id);
            return ResponseEntity.ok(email);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Créer un nouvel email")
    public ResponseEntity<?> createEmail(@RequestBody Email email) {
        try {
            Email savedEmail = emailService.createEmail(email);
            return ResponseEntity.ok(savedEmail);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Mettre à jour un email")
    public ResponseEntity<?> updateEmail(@PathVariable Long id, @RequestBody Email emailDetails) {
        try {
            Email updatedEmail = emailService.updateEmail(id, emailDetails);
            return ResponseEntity.ok(updatedEmail);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un email")
    public ResponseEntity<?> deleteEmail(@PathVariable Long id) {
        try {
            emailService.deleteEmail(id);
            return ResponseEntity.ok(Map.of("message", "Email supprimé", "id", id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Rechercher des emails")
    public ResponseEntity<List<Email>> searchEmails(@RequestParam String q) {
        return ResponseEntity.ok(emailService.searchEmails(q));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Obtenir les statistiques")
    public ResponseEntity<Map<String, Object>> getEmailStats() {
        return ResponseEntity.ok(emailService.getEmailStats());
    }

    @GetMapping("/export/valid")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Exporter les emails valides")
    public ResponseEntity<List<Email>> exportValidEmails() {
        return ResponseEntity.ok(emailService.getValidEmails());
    }

    // Endpoint public pour vérifier que l'API fonctionne
    @GetMapping("/public/health")
    @Operation(summary = "Vérifier la santé de l'API")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "service", "Email Validator API",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}