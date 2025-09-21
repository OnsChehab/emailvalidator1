package com.example.email_validator.service;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class EmailValidationService {

    // Liste des domaines considérés comme obsolètes
    private static final List<String> OBSOLETE_DOMAINS = Arrays.asList(
            "hotmail.com", "yahoo.com", "aol.com", "wanadoo.fr",
            "msn.com", "live.com", "outlook.com", "orange.fr",
            "sfr.fr", "free.fr", "bbox.fr", "laposte.net"
    );

    // Liste des domaines factices/invalides
    private static final List<String> INVALID_DOMAINS = Arrays.asList(
            "invalid.email", "example.com", "example.org", "example.net",
            "example.edu", "example.gov", "example.info", "example.biz",
            "test.com", "test.org", "test.net", "fake.com", "dummy.com"
    );

    public String validateEmail(String email) {
        // Vérification de la syntaxe de base
        if (!isValidEmailFormat(email)) {
            return "erronée";
        }

        // Extraction du domaine
        String domain = extractDomain(email);

        // Vérification si le domaine est invalide/factice
        if (isInvalidDomain(domain)) {
            return "erronée";
        }

        // Vérification si le domaine est obsolète
        if (isObsoleteDomain(domain)) {
            return "obsolète";
        }

        // Vérification supplémentaire pour les emails académiques
        if (isAcademicEmail(domain)) {
            return "valide";
        }

        // Par défaut, considérer comme valide
        return "valide";
    }

    private boolean isValidEmailFormat(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private String extractDomain(String email) {
        try {
            return email.substring(email.indexOf('@') + 1).toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isInvalidDomain(String domain) {
        return INVALID_DOMAINS.contains(domain);
    }

    private boolean isObsoleteDomain(String domain) {
        return OBSOLETE_DOMAINS.contains(domain);
    }

    private boolean isAcademicEmail(String domain) {
        return domain.endsWith(".edu") || domain.endsWith(".ac.ma") ||
                domain.contains("universit") || domain.contains("school") ||
                domain.contains("college") || domain.contains("campus") ||
                domain.endsWith(".edu.fr") || domain.endsWith(".ac.fr");
    }
}