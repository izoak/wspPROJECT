package models;

import enums.ManagerType;

import java.util.Objects;

public class Manager extends Employee {
    private static final long serialVersionUID = 1L;

    private final ManagerType managerType;

    public Manager(String id, String firstName, String lastName, String username, String passwordHash,
                   String department, ManagerType managerType) {
        super(id, firstName, lastName, username, passwordHash, department);
        this.managerType = Objects.requireNonNull(managerType);
    }

    @Override
    public String getRole() {
        return "MANAGER";
    }

    public ManagerType getManagerType() {
        return managerType;
    }
}
