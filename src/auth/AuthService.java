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

/**
 * Provides authentication and session management for the university system.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Registering new users by persisting them to the {@link UniversityRepository}.</li>
 *   <li>Authenticating login attempts against stored password hashes.</li>
 *   <li>Maintaining an in-memory set of active session user IDs.</li>
 *   <li>Hashing raw passwords with SHA-256 before any storage or comparison.</li>
 * </ul>
 *
 * <p>All significant events (successful logins, failures, logouts) are written
 * to the system log via {@link ActionLogger}.
 *
 * <p><strong>Security note:</strong> passwords are hashed with SHA-256 and
 * never stored in plain text. The {@link #hashPassword(String)} method is
 * {@code public static} so that other parts of the system can hash a password
 * before passing it to a constructor.
 *
 * @author Gotei 4
 * @version 1.0
 * @see storage.UniversityRepository
 * @see logging.ActionLogger
 * @see models.User
 */
public class AuthService {

    /**
     * Repository used for user persistence and lookup.
     * Obtained via {@link UniversityRepository#getInstance()} at construction time.
     */
    private final UniversityRepository repository;

    /**
     * Set of user IDs that currently have an active session.
     * Entries are added on login and removed on logout.
     */
    private final Set<String> activeSessions = new HashSet<>();

    /**
     * Constructs an {@code AuthService} backed by the singleton
     * {@link UniversityRepository}.
     */
    public AuthService() {
        this.repository = UniversityRepository.getInstance();
    }

    /**
     * Persists a newly created user in the repository and logs the event.
     *
     * <p>Callers are expected to set the user's password hash before calling
     * this method (use {@link #hashPassword(String)} to produce it).
     *
     * @param user the user to register (must not be {@code null})
     */
    public void register(User user) {
        repository.saveUser(user);
        ActionLogger.log("Registered user " + user.getUsername());
    }
    /**
     * Attempts to authenticate a user by username and raw password.
     *
     * <p>Authentication fails (returns empty) when:
     * <ul>
     *   <li>No user with the given username exists.</li>
     *   <li>The user's account is inactive.</li>
     *   <li>The password does not match the stored hash.</li>
     * </ul>
     *
     * <p>On success, the user's {@code lastLoginAt} is updated, the change is
     * persisted, and a session entry is created for the user's ID.
     *
     * @param username    the login username
     * @param rawPassword the plain-text password provided by the user
     * @return an {@link Optional} containing the authenticated {@link User},
     *         or empty if authentication failed
     */
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
            System.out.println("Your account has been deactivated.");
            if (reason != null && !reason.isBlank()) {
                System.out.println("Reason: " + reason);
            }
            return Optional.empty();
        }

        if (!user.getPasswordHash().equals(hashPassword(rawPassword))) {
            ActionLogger.log("Login failed for username " + username + ": wrong password");
            return Optional.empty();
        }

        user.setLastLoginAt(LocalDateTime.now());
        repository.updateUser(user);
        activeSessions.add(user.getId());
        ActionLogger.log("User logged in: " + username);
        return Optional.of(user);
    }

    /**
     * Ends the active session for the given user.
     * The user's ID is removed from the active sessions set and the event is logged.
     *
     * @param user the user who is logging out (must not be {@code null})
     */
    public void logout(User user) {
        activeSessions.remove(user.getId());
        ActionLogger.log("User logged out: " + user.getUsername());
    }
    /**
     * Returns {@code true} if the given user currently has an active session.
     *
     * @param user the user to check
     * @return {@code true} if a session exists for this user's ID
     */
    public boolean isAuthenticated(User user) {
        return activeSessions.contains(user.getId());
    }

    /**
     * Verifies that the given username and raw password match the stored credentials
     * without starting a session or updating login timestamps.
     *
     * <p>Useful for secondary confirmation dialogs (e.g. "confirm your password to
     * change account settings").
     *
     * @param username    username to look up
     * @param rawPassword raw password to verify
     * @return {@code true} if a user with that username exists and the password matches
     */
    public boolean verifyCredentials(String username, String rawPassword) {
        return repository.findByUsername(username)
                .map(user -> user.getPasswordHash().equals(hashPassword(rawPassword)))
                .orElse(false);
    }
    /**
     * Produces a SHA-256 hex digest of the given plain-text password.
     *
     * <p>Uses UTF-8 encoding before hashing. The result is a 64-character
     * lowercase hexadecimal string.
     *
     * @param rawPassword the plain-text password (must not be {@code null})
     * @return SHA-256 hex string of the password
     * @throws IllegalStateException if the SHA-256 algorithm is unavailable
     *                               on the current JVM (should never happen in practice)
     */
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