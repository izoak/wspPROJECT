package models;

import enums.LessonType;

import java.util.ArrayList;
import java.util.List;

/**
 * Course class from the UML diagram.
 * Attributes: -name: String, -credits: int, -lessons: enum, -teachers: list
 *
 * Business rules:
 *  - A student cannot register for more than 21 credits total
 *  - A student cannot fail more than 3 courses
 */
public class Course {

    public static final int MAX_CREDITS = 21;
    public static final int MAX_FAILS = 3;

    // From diagram: -name: String
    private String name;

    // From diagram: -credits: int
    private int credits;

    // From diagram: -lessons: enum (list of lesson types offered)
    private List<LessonType> lessons;

    // From diagram: -teachers: list
    private List<String> teachers;

    public Course(String name, int credits) {
        this.name = name;
        this.credits = credits;
        this.lessons = new ArrayList<>();
        this.teachers = new ArrayList<>();
    }

    public void addLesson(LessonType lessonType) {
        lessons.add(lessonType);
    }

    public void addTeacher(String teacherName) {
        teachers.add(teacherName);
    }

    // --- Credit validation logic ---

    /**
     * Checks if a student can register for this course given their current credit load.
     * Rule: total credits after registration must not exceed MAX_CREDITS (21).
     */
    public static boolean canRegister(List<Course> currentCourses, Course newCourse) {
        int totalCredits = currentCourses.stream().mapToInt(Course::getCredits).sum();
        return (totalCredits + newCourse.getCredits()) <= MAX_CREDITS;
    }

    /**
     * Checks if a student is allowed to register based on their fail count.
     * Rule: student cannot have more than 3 failed courses.
     */
    public static boolean hasNotExceededFails(int failCount) {
        return failCount < MAX_FAILS;
    }

    // --- Getters & Setters ---

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public List<LessonType> getLessons() { return lessons; }
    public List<String> getTeachers() { return teachers; }

    @Override
    public String toString() {
        return String.format("Course[name=%s, credits=%d, lessons=%s, teachers=%s]",
                name, credits, lessons, teachers);
    }
}