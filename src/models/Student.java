package models;

import observer.GradeObserver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Student extends User implements GradeObserver {
    private static final long serialVersionUID = 1L;

    private final String major;
    private int yearOfStudy;
    private final List<Course> registeredCourses;
    private final Map<String, Mark> marksByCourseName;
    private final Map<String, Integer> ratedTeachers;
    private final List<String> notifications;

    public Student(String id, String firstName, String lastName, String username, String passwordHash,
                   String major, int yearOfStudy) {
        super(id, firstName, lastName, username, passwordHash);
        this.major = Objects.requireNonNull(major);
        this.yearOfStudy = yearOfStudy;
        this.registeredCourses = new ArrayList<>();
        this.marksByCourseName = new LinkedHashMap<>();
        this.ratedTeachers = new LinkedHashMap<>();
        this.notifications = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return "STUDENT";
    }

    public boolean canRegister(Course course) {
        return !registeredCourses.contains(course)
                && Course.canRegister(registeredCourses, course)
                && Course.hasNotExceededFails(getFailedCoursesCount());
    }

    public void approveCourseRegistration(Course course) {
        if (!registeredCourses.contains(course)) {
            registeredCourses.add(course);
        }
    }

    public int getTotalCredits() {
        return registeredCourses.stream().mapToInt(Course::getCredits).sum();
    }

    public void addOrUpdateMark(Mark mark) {
        marksByCourseName.put(mark.getCourseName(), mark);
    }

    public Optional<Mark> getMark(String courseName) {
        return Optional.ofNullable(marksByCourseName.get(courseName));
    }

    public int getFailedCoursesCount() {
        return (int) marksByCourseName.values()
                .stream()
                .filter(mark -> mark.getFinalMark() > 0 && !mark.isPassed())
                .count();
    }

    public void rateTeacher(String teacherUsername, int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        ratedTeachers.put(teacherUsername, rating);
    }

    public double calculateGpa() {
        if (marksByCourseName.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (Mark mark : marksByCourseName.values()) {
            sum += convertToGpa(mark.getTotal());
        }
        return sum / marksByCourseName.size();
    }

    public String getTranscript() {
        StringBuilder builder = new StringBuilder();
        builder.append("Transcript for ").append(getFullName()).append(System.lineSeparator());
        for (Mark mark : marksByCourseName.values()) {
            builder.append(" - ").append(mark.getCourseName())
                    .append(": total=").append(String.format("%.1f", mark.getTotal()))
                    .append(", passed=").append(mark.isPassed())
                    .append(System.lineSeparator());
        }
        builder.append("GPA: ").append(String.format("%.2f", calculateGpa()));
        return builder.toString();
    }

    @Override
    public void onGradeUpdated(String studentId, String courseName, double totalMark) {
        notifications.add("Grade updated for " + courseName + ": total = " + String.format("%.1f", totalMark));
    }

    public String getMajor() {
        return major;
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(int yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public List<Course> getRegisteredCourses() {
        return List.copyOf(registeredCourses);
    }

    public Map<String, Mark> getMarksByCourseName() {
        return Map.copyOf(marksByCourseName);
    }

    public Map<String, Integer> getRatedTeachers() {
        return Map.copyOf(ratedTeachers);
    }

    public List<String> getNotifications() {
        return List.copyOf(notifications);
    }

    private double convertToGpa(double total) {
        if (total >= 95) {
            return 4.0;
        }
        if (total >= 90) {
            return 3.67;
        }
        if (total >= 85) {
            return 3.33;
        }
        if (total >= 80) {
            return 3.0;
        }
        if (total >= 75) {
            return 2.67;
        }
        if (total >= 70) {
            return 2.33;
        }
        if (total >= 65) {
            return 2.0;
        }
        if (total >= 60) {
            return 1.67;
        }
        if (total >= 55) {
            return 1.33;
        }
        if (total >= 50) {
            return 1.0;
        }
        return 0.0;
    }
}
