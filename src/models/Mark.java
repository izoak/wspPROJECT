package models;

import observer.GradeObserver;
import observer.GradeSubject;

import java.util.ArrayList;
import java.util.List;

/**
 * Mark class as seen in the UML diagram.
 * Attributes: att1, att2, final (here finalMark to avoid keyword clash)
 * Implements Observer pattern to notify students when grades are posted.
 *
 * Total = att1 + att2 + final
 * Student fails if total < passing threshold (conventionally < 50 or < 30).
 */
public class Mark implements GradeSubject {

    // From diagram: -att1: double, -att2: double, -final: double
    private double att1;
    private double att2;
    private double finalMark;

    // Context: which student and which course this mark belongs to
    private String studentId;
    private String courseName;

    // Observer list
    private final List<GradeObserver> observers = new ArrayList<>();

    public Mark(String studentId, String courseName) {
        this.studentId = studentId;
        this.courseName = courseName;
    }

    // From diagram: +getTotal()
    public double getTotal() {
        return att1 + att2 + finalMark;
    }

    public boolean isPassed() {
        return getTotal() >= 50; // passing threshold: 50 out of 100
    }

    // --- Observer pattern ---

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

    // --- Setters that trigger notifications ---

    public void setAtt1(double att1) {
        this.att1 = att1;
        notifyObservers();
    }

    public void setAtt2(double att2) {
        this.att2 = att2;
        notifyObservers();
    }

    public void setFinalMark(double finalMark) {
        this.finalMark = finalMark;
        notifyObservers();
    }

    // --- Getters ---

    public double getAtt1() { return att1; }
    public double getAtt2() { return att2; }
    public double getFinalMark() { return finalMark; }
    public String getStudentId() { return studentId; }
    public String getCourseName() { return courseName; }

    @Override
    public String toString() {
        return String.format("Mark[student=%s, course=%s, att1=%.1f, att2=%.1f, final=%.1f, total=%.1f]",
                studentId, courseName, att1, att2, finalMark, getTotal());
    }
}
