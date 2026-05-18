package models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class representing a user in the university system.
 *
 * <p>All user roles (Student, Teacher, Admin, Manager, etc.) extend this class.
 * It provides shared identity fields, account status management, and a
 * natural ordering by username (case-insensitive).
 *
 * <p>Implements {@link Serializable} so that user objects can be persisted
 * to disk via the {@code UniversityRepository}.
 *
 * @author Gotei 4
 * @version 1.0
 * @see models.Student
 * @see models.Teacher
 * @see models.Admin
 */
public abstract class User implements Serializable, Comparable<User> {

    private static final long serialVersionUID = 1L;

    /** Unique identifier for the user (never changes after creation). */
    private final String id;

    private String firstName;
    private String lastName;
    private String username;

    /** SHA-256 hex digest of the user's password. Never stored as plain text. */
    private String passwordHash;

    /** {@code true} while the account is enabled; {@code false} when deactivated. */
    private boolean active;

    /** Timestamp of the most recent successful login, or {@code null} if never logged in. */
    private LocalDateTime lastLoginAt;

    /** Optional human-readable explanation set when an account is deactivated. */
    private String deactivationReason;

    /**
     * Constructs a new User with the given identity credentials.
     * The account is active by default.
     *
     * @param id           unique identifier for this user (must not be {@code null})
     * @param firstName    user's first name
     * @param lastName     user's last name
     * @param username     login username (must be unique across the system)
     * @param passwordHash SHA-256 hex digest of the user's password
     */
    protected User(String id, String firstName, String lastName,
                   String username, String passwordHash) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.active = true;
    }

    /**
     * Returns the role label for this user (e.g. {@code "STUDENT"}, {@code "TEACHER"}).
     * Subclasses must provide a concrete implementation.
     *
     * @return non-null role string identifying the user's type
     */
    public abstract String getRole();

    /**
     * Returns the user's full name as {@code "firstName lastName"}.
     *
     * @return full name string
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns the unique identifier of this user.
     *
     * @return user ID (never {@code null})
     */
    public String getId() { 
        return id; 
    }

    /**
     * Returns the user's first name.
     *
     * @return first name
     */
    public String getFirstName() { 
        return firstName; 
    }

    /**
     * Updates the user's first name.
     *
     * @param firstName new first name (must not be {@code null})
     */
    public void setFirstName(String firstName) { 
        this.firstName = firstName; 
    }

    /**
     * Returns the user's last name.
     *
     * @return last name
     */
    public String getLastName() { 
        return lastName; 
    }

    /**
     * Updates the user's last name.
     *
     * @param lastName new last name (must not be {@code null})
     */
    public void setLastName(String lastName) { 
        this.lastName = lastName; 
    }

    /**
     * Returns the user's login username.
     *
     * @return username
     */
    public String getUsername() { 
        return username; 
    }

    /**
     * Updates the login username. Callers are responsible for ensuring uniqueness.
     *
     * @param username new username
     */
    public void setUsername(String username) { 
        this.username = username; 
    }

    /**
     * Returns the stored password hash (SHA-256 hex).
     *
     * @return password hash string
     */
    public String getPasswordHash() { 
        return passwordHash;
    }

    /**
     * Replaces the stored password hash.
     *
     * @param passwordHash new SHA-256 hex digest
     */
    public void setPasswordHash(String passwordHash) { 
        this.passwordHash = passwordHash;
    }

    /**
     * Returns {@code true} if this account is active and may log in.
     *
     * @return {@code true} if active, {@code false} if deactivated
     */
    public boolean isActive() { 
        return active; 
    }

    /**
     * Enables or disables this account.
     *
     * @param active {@code true} to activate, {@code false} to deactivate
     */
    public void setActive(boolean active) { 
        this.active = active;
    }

    /**
     * Returns the timestamp of the last successful login.
     *
     * @return last login time, or {@code null} if the user has never logged in
     */
    public LocalDateTime getLastLoginAt() { 
        return lastLoginAt; 
    }

    /**
     * Records the time of the most recent login. Typically set by {@code AuthService}.
     *
     * @param lastLoginAt login timestamp
     */
    public void setLastLoginAt(LocalDateTime lastLoginAt) { 
        this.lastLoginAt = lastLoginAt; 
    }

    /**
     * Returns the reason this account was deactivated, if any.
     *
     * @return deactivation reason, or {@code null} if none was provided
     */
    public String getDeactivationReason() { 
        return deactivationReason; 
    }

    /**
     * Sets a human-readable explanation for why the account was deactivated.
     * This message is shown to the user on a failed login attempt.
     *
     * @param deactivationReason explanation text (may be {@code null})
     */
    public void setDeactivationReason(String deactivationReason) {
        this.deactivationReason = deactivationReason;
    }

    /**
     * Compares this user to another by username (case-insensitive alphabetical order).
     *
     * @param other the other user
     * @return negative, zero, or positive as this username comes before,
     *         equals, or comes after the other username
     */
    @Override
    public int compareTo(User other) {
        return this.username.compareToIgnoreCase(other.username);
    }

    /**
     * Two users are equal if and only if their IDs are equal.
     *
     * @param o object to compare
     * @return {@code true} if {@code o} is a {@code User} with the same ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id.equals(user.id);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() { return Objects.hash(id); }

    /**
     * Returns a concise string representation of this user for debugging.
     *
     * @return string with id, full name, username, role, active flag, and last login
     */
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", username='" + username + '\'' +
                ", role='" + getRole() + '\'' +
                ", active=" + active +
                ", lastLoginAt=" + lastLoginAt +
                '}';
    }
}