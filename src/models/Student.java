package models;

import observer.GradeObserver;

import java.util.*;

/**
 * Represents an enrolled student in the university system.
 *
 * <p>A {@code Student} extends {@link User} and additionally:
 * <ul>
 *   <li>Tracks registered {@link Course}s and the associated {@link Mark}s.</li>
 *   <li>Calculates a 4.0-scale GPA from accumulated marks.</li>
 *   <li>Implements {@link GradeObserver} to receive real-time grade notifications.</li>
 *   <li>Enforces academic rules: maximum credit load ({@value Course#MAX_CREDITS})
 *       and maximum number of failed courses ({@value Course#MAX_FAILS}).</li>
 * </ul>
 *
 * @author Gotei 4
 * @version 1.0
 * @see models.Course
 * @see models.Mark
 * @see observer.GradeObserver
 */
public class Student extends User implements GradeObserver {

    private static final long serialVersionUID = 1L;

    /** Academic department / discipline of the student (immutable after creation). */
    private final String major;

    /** Current year of study (1-based). */
    private int yearOfStudy;

    /** Courses the student has been approved to attend. */
    private final List<Course> registeredCourses;

    /**
     * Maps course name → latest {@link Mark} for that course.
     * A {@link LinkedHashMap} preserves insertion order for transcript printing.
     */
    private final Map<String, Mark> marksByCourseName;

    /**
     * Maps teacher username → rating (1–5) given by this student.
     * Each teacher can be rated at most once per student.
     */
    private final Map<String, Integer> ratedTeachers;

    /** In-memory notification messages received via the observer mechanism. */
    private final List<String> notifications;

    /**
     * Constructs a new Student with the given identity and academic details.
     *
     * @param id           unique user identifier
     * @param firstName    student's first name
     * @param lastName     student's last name
     * @param username     login username
     * @param passwordHash SHA-256 hex digest of the password
     * @param major        academic major / department
     * @param yearOfStudy  current year of study (must be &ge; 1)
     */
    public Student(String id, String firstName, String lastName,
                   String username, String passwordHash,
                   String major, int yearOfStudy) {
        super(id, firstName, lastName, username, passwordHash);
        this.major = major;
        this.yearOfStudy = yearOfStudy;
        this.registeredCourses = new ArrayList<>();
        this.marksByCourseName = new LinkedHashMap<>();
        this.ratedTeachers = new LinkedHashMap<>();
        this.notifications = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     *
     * @return always {@code "STUDENT"}
     */
    @Override
    public String getRole() { 
        return "STUDENT"; 
    }

    /**
     * Checks whether this student is eligible to register for the given course.
     *
     * <p>Registration is allowed when all three conditions hold:
     * <ol>
     *   <li>The student is not already registered for {@code course}.</li>
     *   <li>Adding the course's credits would not exceed {@value Course#MAX_CREDITS}.</li>
     *   <li>The student has fewer than {@value Course#MAX_FAILS} failed courses.</li>
     * </ol>
     *
     * @param course the course to check
     * @return {@code true} if registration is permitted
     */
    public boolean canRegister(Course course) {
        return !registeredCourses.contains(course)
                && Course.canRegister(registeredCourses, course)
                && Course.hasNotExceededFails(getFailedCoursesCount());
    }

    /**
     * Adds the course to this student's registered list without any eligibility checks.
     * Use only after approval has already been verified (e.g., by {@link #canRegister}).
     *
     * @param course the course to register (ignored if already registered)
     */
    public void approveCourseRegistration(Course course) {
        if (!registeredCourses.contains(course)) {
            registeredCourses.add(course);
        }
    }

    /**
     * Returns the total number of credit hours across all registered courses.
     *
     * @return sum of credits, or {@code 0} if no courses are registered
     */
    public int getTotalCredits() {
        return registeredCourses.stream().mapToInt(Course::getCredits).sum();
    }

    /**
     * Stores or replaces the {@link Mark} for a course.
     * The mark is keyed by its course name.
     *
     * @param mark the mark to store (must not be {@code null})
     */
    public void addOrUpdateMark(Mark mark) {
        marksByCourseName.put(mark.getCourseName(), mark);
    }

    /**
     * Looks up the mark for the given course.
     *
     * @param courseName name of the course
     * @return an {@link Optional} containing the mark, or empty if no mark exists
     */
    public Optional<Mark> getMark(String courseName) {
        return Optional.ofNullable(marksByCourseName.get(courseName));
    }

    /**
     * Counts the courses in which this student has received a final mark but did not pass.
     *
     * @return number of failed courses
     */
    public int getFailedCoursesCount() {
        return (int) marksByCourseName.values().stream()
                .filter(mark -> mark.getFinalMark() > 0 && !mark.isPassed())
                .count();
    }

    /**
     * Records this student's rating for a teacher.
     * Calling this method again for the same teacher overwrites the previous rating.
     *
     * @param teacherUsername the username of the teacher being rated
     * @param rating          integer rating from 1 (lowest) to 5 (highest)
     * @throws IllegalArgumentException if {@code rating} is not in [1, 5]
     */
    public void rateTeacher(String teacherUsername, int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        ratedTeachers.put(teacherUsername, rating);
    }

    /**
     * Calculates and returns the student's current GPA on a 4.0 scale.
     *
     * <p>Each recorded mark is individually converted to the 4.0 scale with
     * {@link #convertToGpa(double)}, and the results are averaged.
     *
     * @return GPA in [0.0, 4.0], or {@code 0.0} if no marks are recorded
     */
    public double calculateGpa() {
        if (marksByCourseName.isEmpty()) return 0.0;

        double sum = 0.0;
        for (Mark mark : marksByCourseName.values()) {
            sum += convertToGpa(mark.getTotal());
        }
        return sum / marksByCourseName.size();
    }

    /**
     * Builds a formatted transcript listing every course with its total
     * mark, pass/fail status, and the overall GPA at the bottom.
     *
     * @return multi-line transcript string
     */
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

    /**
     * Called by the grading subsystem whenever a total mark is updated for this student.
     * Stores a human-readable notification message in the student's notification list.
     *
     * @param studentId  ID of the student whose grade changed
     * @param courseName name of the affected course
     * @param totalMark  updated total score (0–100)
     */
    @Override
    public void onGradeUpdated(String studentId, String courseName, double totalMark) {
        notifications.add("Grade updated for " + courseName
                + ": total = " + String.format("%.1f", totalMark));
    }

    /**
     * Returns the student's academic major.
     *
     * @return major string (e.g. {@code "Computer Science"})
     */
    public String getMajor() { 
        return major; 
    }

    /**
     * Returns the student's current year of study.
     *
     * @return year (1-based)
     */
    public int getYearOfStudy() { 
        return yearOfStudy; 
    }

    /**
     * Updates the year of study (e.g. after promotion).
     *
     * @param yearOfStudy new year value
     */
    public void setYearOfStudy(int yearOfStudy) { 
        this.yearOfStudy = yearOfStudy; 
    }

    /**
     * Returns an unmodifiable snapshot of the student's registered courses.
     *
     * @return list of registered courses (never {@code null})
     */
    public List<Course> getRegisteredCourses() { 
        return List.copyOf(registeredCourses); 
    }

    /**
     * Returns an unmodifiable snapshot of the student's marks, keyed by course name.
     *
     * @return map of course name → mark
     */
    public Map<String, Mark> getMarksByCourseName() { 
        return Map.copyOf(marksByCourseName); 
    }

    /**
     * Returns an unmodifiable snapshot of teacher ratings given by this student.
     *
     * @return map of teacher username → rating (1–5)
     */
    public Map<String, Integer> getRatedTeachers() { 
        return Map.copyOf(ratedTeachers); 
    }

    /**
     * Returns an unmodifiable snapshot of all grade-update notifications received.
     *
     * @return list of notification strings
     */
    public List<String> getNotifications() { 
        return List.copyOf(notifications); 
    }

    // ------------------------------------------------------------------ //
    //  Private helpers                                                      //
    // ------------------------------------------------------------------ //

    /**
     * Converts a 100-point total score to the 4.0 GPA scale
     * using standard letter-grade thresholds.
     *
     * @param total raw score in [0, 100]
     * @return GPA value in [0.0, 4.0]
     */
    private double convertToGpa(double total) {
        if (total >= 95) return 4.0;
        if (total >= 90) return 3.67;
        if (total >= 85) return 3.33;
        if (total >= 80) return 3.0;
        if (total >= 75) return 2.67;
        if (total >= 70) return 2.33;
        if (total >= 65) return 2.0;
        if (total >= 60) return 1.67;
        if (total >= 55) return 1.33;
        if (total >= 50) return 1.0;
        return 0.0;
    }
}