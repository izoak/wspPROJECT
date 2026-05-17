package models;

import observer.GradeObserver;
import observer.GradeSubject;
import observer.StudentNotificationService;
import java.util.ArrayList;
import java.util.List;


public class Mark implements GradeSubject, java.io.Serializable {
	private static final long serialVersionUID = 1L;
    private double att1;
    private double att2;
    private double finalMark;

    private String studentId;
    private String courseName;

    private final List<GradeObserver> observers = new ArrayList<>();

    public Mark(String studentId, String courseName) {
        this.studentId = studentId;
        this.courseName = courseName;
    }

    public double getTotal() {
        return att1 + att2 + finalMark;
    }

    public boolean isPassed() {
        return getTotal() >= 50;
    }


    @Override
    public void addObserver(GradeObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(GradeObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (GradeObserver observer : observers) {
            observer.onGradeUpdated(studentId, courseName, getTotal());
        }
    }

    public void setAtt1(double att1) {
        validateComponent(att1, 30, "Attestation 1");
        this.att1 = att1;
        for (GradeObserver observer : observers) {
            if (observer instanceof StudentNotificationService sns) {
                sns.onAtt1Updated(studentId, courseName, this.att1);
            } else {
                observer.onGradeUpdated(studentId, courseName, getTotal());
            }
        }
    }

    public void setAtt2(double att2) {
        validateComponent(att2, 30, "Attestation 2");
        this.att2 = att2;
        for (GradeObserver observer : observers) {
            if (observer instanceof StudentNotificationService sns) {
                sns.onAtt2Updated(studentId, courseName, this.att1, this.att2);
            } else {
                observer.onGradeUpdated(studentId, courseName, getTotal());
            }
        }
    }

    public void setFinalMark(double finalMark) {
        validateComponent(finalMark, 40, "Final exam");
        this.finalMark = finalMark;
        for (GradeObserver observer : observers) {
            if (observer instanceof StudentNotificationService sns) {
                sns.onFinalUpdated(studentId, courseName, this.att1, this.att2, this.finalMark);
            } else {
                observer.onGradeUpdated(studentId, courseName, getTotal());
            }
        }
    }

    public double getAtt1() { return att1; }
    public double getAtt2() { return att2; }
    public double getFinalMark() { return finalMark; }
    public String getStudentId() { return studentId; }
    public String getCourseName() { return courseName; }

    private void validateComponent(double value, double maxValue, String componentName) {
        if (value < 0 || value > maxValue) {
            throw new IllegalArgumentException(componentName + " must be between 0 and " + maxValue);
        }
    }

    @Override
    public String toString() {
        return String.format("Mark[student=%s, course=%s, att1=%.1f, att2=%.1f, final=%.1f, total=%.1f]",
                studentId, courseName, att1, att2, finalMark, getTotal());
    }
}
