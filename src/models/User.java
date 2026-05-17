package models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class User implements Serializable, Comparable<User> {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String firstName;
    private String lastName;
    private String username;
    private String passwordHash;
    private boolean active;
    private LocalDateTime lastLoginAt;
    private String deactivationReason;

    protected User(String id, String firstName, String lastName, String username, String passwordHash) {
        this.id = Objects.requireNonNull(id);
        this.firstName = Objects.requireNonNull(firstName);
        this.lastName = Objects.requireNonNull(lastName);
        this.username = Objects.requireNonNull(username);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.active = true;
    }

    public abstract String getRole();

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = Objects.requireNonNull(firstName);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = Objects.requireNonNull(lastName);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = Objects.requireNonNull(username);
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public String getDeactivationReason() {
    	return deactivationReason;
    }
    
    public void setDeactivationReason(String deactivationReason ) {
    	this.deactivationReason = deactivationReason;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    @Override
    public int compareTo(User other) {
        return this.username.compareToIgnoreCase(other.username);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User user)) {
            return false;
        }
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

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
