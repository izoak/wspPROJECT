package models;

import enums.LessonType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an academic course offered by the university.
 *
 * <p>A {@code Course} has a unique identifier, a credit value, an optional
 * list of {@link LessonType}s that make up its schedule, and a list of
 * teacher names responsible for it.
 *
 * <p>Two static rule-enforcement helpers are provided:
 * <ul>
 *   <li>{@link #canRegister(List, Course)} — checks the credit-hour limit.</li>
 *   <li>{@link #hasNotExceededFails(int)} — checks the failure limit.</li>
 * </ul>
 *
 * <p>Equality and hashing are based solely on {@code courseId}, so two
 * {@code Course} objects with the same ID are considered the same course
 * regardless of other fields.
 *
 * @author Gotei 4
 * @version 1.0
 * @see models.Student
 * @see enums.LessonType
 */
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Maximum total credit hours a student may register for in one semester.
     *
     * @see #canRegister(List, Course)
     */
    public static final int MAX_CREDITS = 21;

    /**
     * Maximum number of failed courses a student may accumulate before
     * further course registration is blocked.
     *
     * @see #hasNotExceededFails(int)
     */
    public static final int MAX_FAILS = 3;

    /** Unique identifier for this course (e.g. {@code "COMP"} or {@code "MATH101"}). */
    private final String courseId;

    /** Human-readable course name (e.g. {@code "Computer Networks"}). */
    private String name;

    /** Number of credit hours this course is worth. */
    private int credits;

    /** Ordered list of lesson formats offered in this course (lecture, lab, etc.). */
    private List<LessonType> lessons;

    /** Usernames or display names of teachers assigned to this course. */
    private List<String> teachers;

    /**
     * Convenience constructor that auto-generates the course ID from the first
     * four characters of the name, upper-cased.
     *
     * <p>Example: {@code new Course("Computer Networks", 3)} produces ID {@code "COMP"}.
     *
     * @param name    full course name (must have at least one character)
     * @param credits credit-hour value (should be &gt; 0)
     */
    public Course(String name, int credits) {
        this(name.substring(0, Math.min(4, name.length())).toUpperCase(), name, credits);
    }

    /**
     * Full constructor with an explicit course ID.
     *
     * @param courseId unique course identifier (must not be {@code null})
     * @param name     full course name
     * @param credits  credit-hour value
     * @throws NullPointerException if {@code courseId} is {@code null}
     */
    public Course(String courseId, String name, int credits) {
        this.courseId = Objects.requireNonNull(courseId, "courseId must not be null");
        this.name = name;
        this.credits = credits;
        this.lessons = new ArrayList<>();
        this.teachers = new ArrayList<>();
    }

    /**
     * Adds a lesson type to this course's lesson schedule.
     *
     * @param lessonType the type of lesson to add (e.g. LECTURE, LAB)
     */
    public void addLesson(LessonType lessonType) {
        lessons.add(lessonType);
    }

    /**
     * Associates a teacher with this course.
     *
     * @param teacherName display name or username of the teacher
     */
    public void addTeacher(String teacherName) {
        teachers.add(teacherName);
    }

    /**
     * Returns {@code true} if adding {@code newCourse} would keep the student
     * within the {@value #MAX_CREDITS}-credit-hour semester limit.
     *
     * @param currentCourses courses the student is already registered for
     * @param newCourse      the candidate course to add
     * @return {@code true} if the combined credit total does not exceed {@value #MAX_CREDITS}
     */
    public static boolean canRegister(List<Course> currentCourses, Course newCourse) {
        int totalCredits = currentCourses.stream().mapToInt(Course::getCredits).sum();
        return (totalCredits + newCourse.getCredits()) <= MAX_CREDITS;
    }

    /**
     * Returns {@code true} if the student has not yet reached the
     * maximum number of course failures ({@value #MAX_FAILS}).
     *
     * @param failCount number of courses the student has already failed
     * @return {@code true} if {@code failCount < MAX_FAILS}
     */
    public static boolean hasNotExceededFails(int failCount) {
        return failCount < MAX_FAILS;
    }

    /**
     * Returns the unique course identifier.
     *
     * @return course ID (never {@code null})
     */
    public String getCourseId() { 
        return courseId; 
    }

    /**
     * Returns the full course name.
     *
     * @return course name
     */
    public String getName() { 
        return name; 
    }

    /**
     * Updates the course name.
     *
     * @param name new name (must not be {@code null})
     */
    public void setName(String name) { 
        this.name = name; 
    }

    /**
     * Returns the credit-hour value of this course.
     *
     * @return number of credits
     */
    public int getCredits() { 
        return credits; 
    }

    /**
     * Updates the credit-hour value.
     *
     * @param credits new credit value (should be &gt; 0)
     */
    public void setCredits(int credits) { 
        this.credits = credits; 
    }

    /**
     * Returns the list of lesson types scheduled for this course.
     *
     * @return mutable list of {@link LessonType}s
     */
    public List<LessonType> getLessons() { 
        return lessons; 
    }

    /**
     * Returns the list of teacher names assigned to this course.
     *
     * @return mutable list of teacher names / usernames
     */
    public List<String> getTeachers() { 
        return teachers; 
    }

    /**
     * Two courses are equal if and only if they share the same {@code courseId}.
     *
     * @param o object to compare
     * @return {@code true} if {@code o} is a {@code Course} with the same ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course course)) return false;
        return courseId.equals(course.courseId);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() { 
        return Objects.hash(courseId); 
    }

    /**
     * Returns a summary string including ID, name, credits, teachers, and lesson types.
     *
     * @return human-readable course description
     */
    @Override
    public String toString() {
        return String.format("%s - %s (%d credits, teachers=%s, lessons=%s)",
                courseId, name, credits, teachers, lessons);
    }
}