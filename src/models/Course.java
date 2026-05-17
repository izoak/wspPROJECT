package models;

import enums.LessonType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_CREDITS = 21;
    public static final int MAX_FAILS = 3;

    private final String courseId;

    private String name;

    private int credits;

    private List<LessonType> lessons;

    private List<String> teachers;

    public Course(String name, int credits) {
        this(name.substring(0, Math.min(4, name.length())).toUpperCase(), name, credits);
    }

    public Course(String courseId, String name, int credits) {
        this.courseId = Objects.requireNonNull(courseId);
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


    public static boolean canRegister(List<Course> currentCourses, Course newCourse) {
        int totalCredits = currentCourses.stream().mapToInt(Course::getCredits).sum();
        return (totalCredits + newCourse.getCredits()) <= MAX_CREDITS;
    }


    public static boolean hasNotExceededFails(int failCount) {
        return failCount < MAX_FAILS;
    }


    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }

    public String getCourseId() { 
        return courseId; 
    }

    public int getCredits() { 
        return credits; 
    }
    public void setCredits(int credits) { 
        this.credits = credits; 
    }

    public List<LessonType> getLessons() { 
        return lessons; 
    }
    public List<String> getTeachers() { 
        return teachers; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Course course)) {
            return false;
        }
        return courseId.equals(course.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId);
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%d credits, teachers=%s, lessons=%s)",
                courseId, name, credits, teachers, lessons);
    }
}
