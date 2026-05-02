package observer;

/**
 * Observer interface for the Observer pattern.
 * Used to notify students about their marks.
 */
public interface GradeObserver {
    void onGradeUpdated(String studentId, String courseName, double totalMark);
}
