package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Employee extends User {
    private static final long serialVersionUID = 1L;

    private String department;
    private final List<String> inbox;

    protected Employee(String id, String firstName, String lastName, String username, String passwordHash,
                       String department) {
        super(id, firstName, lastName, username, passwordHash);
        this.department = Objects.requireNonNull(department);
        this.inbox = new ArrayList<>();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = Objects.requireNonNull(department);
    }

    public void sendMessage(Employee recipient, String message) {
        recipient.receiveMessage("From " + getFullName() + ": " + message);
    }

    public void receiveMessage(String message) {
        inbox.add(message);
    }

    public List<String> getInbox() {
        return List.copyOf(inbox);
    }
}
