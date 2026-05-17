package models;

import enums.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Teacher extends Employee {
    private static final long serialVersionUID = 1L;

    private final Position position;
    private final List<Course> assignedCourses;
    private final List<Integer> ratings;

    public Teacher(String id, String firstName, String lastName, String username, String passwordHash,
                   String department, Position position) {
        super(id, firstName, lastName, username, passwordHash, department);
        this.position = Objects.requireNonNull(position);
        this.assignedCourses = new ArrayList<>();
        this.ratings = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return "TEACHER";
    }

    public void assignCourse(Course course) {
        if (!assignedCourses.contains(course)) {
            assignedCourses.add(course);
        }
    }

    public boolean teachesCourse(Course course) {
        return assignedCourses.contains(course);
    }

    public void addRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        ratings.add(rating);
    }

    public double getAverageRating() {
        return ratings.isEmpty()
                ? 0.0
                : ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    public Position getPosition() {
        return position;
    }

    public List<Course> getAssignedCourses() {
        return List.copyOf(assignedCourses);
    }
}
