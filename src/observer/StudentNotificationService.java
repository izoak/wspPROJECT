package observer;
public class StudentNotificationService implements GradeObserver {

    private final String studentName;

    public StudentNotificationService(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public void onGradeUpdated(String studentId, String courseName, double totalMark) {
    }

    public void onAtt1Updated(String studentId, String courseName, double att1) {
        System.out.printf("[%s] First attestation: %.1f / 30%n", studentName, att1);
    }

    public void onAtt2Updated(String studentId, String courseName, double att1, double att2) {
        System.out.printf("[%s] Second attestation: %.1f / 30%n", studentName, att2);
    }

    public void onFinalUpdated(String studentId, String courseName, double att1, double att2, double finalMark) {
        double total = att1 + att2 + finalMark;
        System.out.printf("[%s] Final exam: %.1f / 40%n", studentName, finalMark);
        System.out.printf("[%s] Total: %.1f/30 + %.1f/30 + %.1f/40 = %.1f/100 — %s%n",
                studentName, att1, att2, finalMark, total,
                total >= 50 ? "PASSED" : "FAILED");
    }
}

