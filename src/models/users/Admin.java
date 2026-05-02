package university.models.users;

import university.database.DataStorage;
import university.enums.Gender;
import university.models.academic.LogEntry;
import university.models.base.Employee;
import university.models.base.User;

import java.util.List;

public class Admin extends Employee {

    private static final long serialVersionUID = 1L;

    public Admin(String name, String email, String password, Gender gender,
                 String employeeID, double salary, String department) {
        super(name, email, password, gender, employeeID, salary, department);
    }

    public void addUser(User user) {
        DataStorage.getInstance().addUser(user);
        log("Added user: " + user.getName());
    }

    public void removeUser(String userId) {
        boolean removed = DataStorage.getInstance().removeUser(userId);
        log(removed ? "Removed user: " + userId : "Failed to remove user: " + userId);
    }

    public void updateUser(User user) {
        DataStorage.getInstance().updateUser(user);
        log("Updated user: " + user.getName());
    }

    public void resetPassword(String userId, String newPassword) {
        DataStorage.getInstance().getUserById(userId).ifPresentOrElse(
                user -> {
                    user.setPassword(newPassword);
                    DataStorage.getInstance().updateUser(user);
                    log("Reset password for: " + user.getName());
                },
                () -> System.out.println("[ADMIN] User not found: " + userId)
        );
    }

    public void viewLogs() {
        System.out.println("═══════════════ SYSTEM LOGS ═══════════════");
        List<LogEntry> logs = DataStorage.getInstance().getLogEntries();
        if (logs.isEmpty()) {
            System.out.println("  No log entries.");
        } else {
            logs.forEach(System.out::println);
        }
        System.out.println("═══════════════════════════════════════════");
    }

    private void log(String action) {
        DataStorage.getInstance().log(getUserID(), getName(), action);
        System.out.println("[ADMIN] " + action);
    }

    @Override
    public String toString() {
        return String.format("Admin{name='%s', dept='%s'}", getName(), getDepartment());
    }
}