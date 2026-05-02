package observer;


/**
 * Concrete Observer implementation.
 * Simulates a student receiving a grade notification.
 *
 * This is the Observer pattern in action:
 *  - GradeSubject = Mark
 *  - GradeObserver = StudentNotificationService (this class)
 */
public class StudentNotificationService implements GradeObserver {

    private final String studentName;

    public StudentNotificationService(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public void onGradeUpdated(String studentId, String courseName, double totalMark) {
        System.out.printf("[NOTIFICATION] Student '%s' (ID: %s): your grade for '%s' has been updated. Total: %.1f%n",
                studentName, studentId, courseName, totalMark);

        if (totalMark < 50) {
            System.out.printf("[WARNING] You are currently FAILING '%s'. Total: %.1f/100%n", courseName, totalMark);
        } else {
            System.out.printf("[INFO] You are PASSING '%s'. Total: %.1f/100%n", courseName, totalMark);
        }
    }
}

