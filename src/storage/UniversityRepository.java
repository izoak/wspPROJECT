package storage;

import models.User;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class UniversityRepository implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Path STORAGE_DIRECTORY = Path.of("data");
    private static final Path STORAGE_FILE = STORAGE_DIRECTORY.resolve("university-repository.ser");

    private static UniversityRepository instance;

    private final Map<String, User> usersById;

    private UniversityRepository() {
        this.usersById = new LinkedHashMap<>();
    }

    public static synchronized UniversityRepository getInstance() {
        if (instance == null) {
            instance = loadFromDisk();
        }
        return instance;
    }

    public synchronized void saveUser(User user) {
        ensureUniqueUsername(user);
        usersById.put(user.getId(), user);
        persist();
    }

    public synchronized void updateUser(User user) {
        User existing = usersById.get(user.getId());
        if (existing == null) {
            throw new IllegalArgumentException("User not found: " + user.getId());
        }

        Optional<User> conflictingUser = findByUsername(user.getUsername())
                .filter(found -> !found.getId().equals(user.getId()));
        if (conflictingUser.isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        usersById.put(user.getId(), user);
        persist();
    }

    public synchronized boolean removeUser(String userId) {
        User removed = usersById.remove(userId);
        if (removed != null) {
            persist();
            return true;
        }
        return false;
    }

    public synchronized Optional<User> findById(String userId) {
        return Optional.ofNullable(usersById.get(userId));
    }

    public synchronized Optional<User> findByUsername(String username) {
        return usersById.values()
                .stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public synchronized Collection<User> getAllUsers() {
        return new ArrayList<>(usersById.values());
    }

    private void ensureUniqueUsername(User user) {
        Optional<User> existing = findByUsername(user.getUsername());
        if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
    }

    private synchronized void persist() {
        try {
            Files.createDirectories(STORAGE_DIRECTORY);
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(STORAGE_FILE.toFile()))) {
                outputStream.writeObject(this);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to persist repository", e);
        }
    }

    private static UniversityRepository loadFromDisk() {
        if (!Files.exists(STORAGE_FILE)) {
            return new UniversityRepository();
        }

        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(STORAGE_FILE.toFile()))) {
            Object loaded = inputStream.readObject();
            if (loaded instanceof UniversityRepository repository) {
                return repository;
            }
        } catch (IOException | ClassNotFoundException e) {
            return new UniversityRepository();
        }

        return new UniversityRepository();
    }
}
