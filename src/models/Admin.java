package models;

import java.util.ArrayList;
import java.util.List;

import logging.ActionLogger;
import storage.UniversityRepository;

public class Admin extends Employee {
    private static final long serialVersionUID = 1L;

    public Admin(String id, String firstName, String lastName, String username, String passwordHash, String department) {
        super(id, firstName, lastName, username, passwordHash, department);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    public void addUser(User user) {
        UniversityRepository.getInstance().saveUser(user);
        ActionLogger.log(getUsername() + " added user " + user.getUsername());
    }

    public void updateUser(User user) {
        UniversityRepository.getInstance().updateUser(user);
        ActionLogger.log(getUsername() + " updated user " + user.getUsername());
    }

    public boolean removeUser(String userId) {
        boolean removed = UniversityRepository.getInstance().removeUser(userId);
        ActionLogger.log(getUsername() + " removed user with id " + userId + ": " + removed);
        return removed;
    }

    public List<String> viewLogs() {
        return new ArrayList<>(ActionLogger.readLogs());
    }
}
