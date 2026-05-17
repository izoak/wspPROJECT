package observer;


public interface GradeObserver {
    void onGradeUpdated(String studentId, String courseName, double totalMark);
}
