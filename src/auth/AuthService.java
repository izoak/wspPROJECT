package auth;

import logging.ActionLogger;
import models.User;
import storage.UniversityRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class AuthService {
    private final UniversityRepository repository;
    private final Set<String> activeSessions = new HashSet<>();

    public AuthService() {
        this.repository = UniversityRepository.getInstance();
    }

    public void register(User user) {
        repository.saveUser(user);
        ActionLogger.log("Registered user " + user.getUsername());
    }

    public Optional<User> login(String username, String rawPassword) {
        Optional<User> userOptional = repository.findByUsername(username);
        if (userOptional.isEmpty()) {
            ActionLogger.log("Login failed for username " + username + ": user not found");
            return Optional.empty();
        }

        User user = userOptional.get();
        if (!user.isActive()) {
            String reason = user.getDeactivationReason();
            ActionLogger.log("Login failed for username " + username + ": account inactive");
            System.out.println("⛔ Your account has been deactivated.");
            if (reason != null && !reason.isBlank()) {
                System.out.println("Reason: " + reason);
            }
            return Optional.empty();
        }

        String passwordHash = hashPassword(rawPassword);
        if (!user.getPasswordHash().equals(passwordHash)) {
            ActionLogger.log("Login failed for username " + username + ": wrong password");
            return Optional.empty();
        }

        user.setLastLoginAt(LocalDateTime.now());
        repository.updateUser(user);
        activeSessions.add(user.getId());
        ActionLogger.log("User logged in: " + username);
        return Optional.of(user);
    }

    public void logout(User user) {
        activeSessions.remove(user.getId());
        ActionLogger.log("User logged out: " + user.getUsername());
    }

    public boolean isAuthenticated(User user) {
        return activeSessions.contains(user.getId());
    }

    public boolean verifyCredentials(String username, String rawPassword) {
        return repository.findByUsername(username)
                .map(user -> user.getPasswordHash().equals(hashPassword(rawPassword)))
                .orElse(false);
    }

    public static String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
