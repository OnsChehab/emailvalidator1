package com.example.email_validator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API d'authentification")
@CrossOrigin(origins = "*")
public class AuthController {

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur")
    public ResponseEntity<Map<String, Object>> login(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Connexion réussie");
        response.put("username", authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion utilisateur")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Déconnexion réussie");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    @Operation(summary = "Obtenir les informations utilisateur")
    public ResponseEntity<Map<String, Object>> getUserInfo(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication != null && authentication.isAuthenticated()) {
            response.put("authenticated", true);
            response.put("username", authentication.getName());
        } else {
            response.put("authenticated", false);
        }
        return ResponseEntity.ok(response);
    }
}