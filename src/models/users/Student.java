package university.models.users;

import university.enums.Gender;
import university.exceptions.MaxCreditsExceededException;
import university.exceptions.MaxFailsExceededException;
import university.exceptions.PrerequisiteNotMetException;
import university.models.academic.Course;
import university.models.academic.Mark;
import university.models.base.User;

import java.util.*;

public class Student extends User {

    private static final long serialVersionUID = 1L;

    private final String studentId;
    private double gpa;
    private int yearOfStudy;
    private String major;

    private final Map<String, Course> enrolledCourses = new LinkedHashMap<>();
    private final Map<String, Mark> allMarks = new LinkedHashMap<>();
    private final Map<String, Integer> failCounts = new HashMap<>();
    private final Set<String> completedCourseIds = new HashSet<>();

    public Student(String name, String email, String password, Gender gender,
                   String studentId, int yearOfStudy, String major) {
        super(name, email, password, gender);
        this.studentId = studentId;
        this.gpa = 0.0;
        this.yearOfStudy = yearOfStudy;
        this.major = major;
    }

    public void registerForCourse(Course course)
            throws MaxCreditsExceededException, MaxFailsExceededException,
            PrerequisiteNotMetException {

        if (enrolledCourses.containsKey(course.getCourseId())) {
            System.out.println("[STUDENT] Already enrolled in " + course.getName());
            return;
        }

        course.enrollStudent(
                this.studentId,
                this.getName(),
                getCurrentCreditLoad(),
                completedCourseIds
        );

        enrolledCourses.put(course.getCourseId(), course);
    }

    public int getCurrentCreditLoad() {
        return enrolledCourses.values().stream()
                .mapToInt(Course::getCredits)
                .sum();
    }

    public Mark getMarkForCourse(String courseId) {
        Course course = enrolledCourses.get(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Not enrolled in course: " + courseId);
        }
        return course.getMark(studentId);
    }

    public void viewMarks() {
        System.out.println("═══════ Marks for " + getName() + " ═══════");
        enrolledCourses.forEach((id, course) -> {
            try {
                System.out.println("  " + course.getMark(studentId));
            } catch (Exception ignored) {}
        });
    }

    public void finaliseCompletedCourse(String courseId) {
        Course course = enrolledCourses.get(courseId);
        if (course == null) return;

        Mark mark = course.getMark(studentId);
        if (!mark.isComplete()) return;

        if (mark.isPassed()) {
            completedCourseIds.add(courseId);
            enrolledCourses.remove(courseId);
        } else {
            failCounts.merge(courseId, 1, Integer::sum);
        }

        allMarks.put(courseId, mark);
        recalculateGpa();
    }

    private void recalculateGpa() {
        OptionalDouble avg = allMarks.values().stream()
                .filter(Mark::isComplete)
                .mapToDouble(Mark::getGpaPoints)
                .average();

        this.gpa = avg.orElse(0.0);
    }

    public void getTranscript() {
        System.out.println("══════════════════════════════════════════════════");
        System.out.printf("TRANSCRIPT — %s (%s)%n", getName(), studentId);
        System.out.printf("Major: %s | Year: %d | GPA: %.2f%n", major, yearOfStudy, gpa);
        System.out.println("──────────────────────────────────────────────────");

        System.out.printf("%-30s %6s %5s %6s %7s %6s %7s%n",
                "Course", "ATT1", "ATT2", "FINAL", "TOTAL", "GRADE", "STATUS");

        allMarks.values().forEach(m -> System.out.printf(
                "%-30s %6s %5s %6s %7.2f %6s %7s%n",
                m.getCourseName(),
                m.getAtt1() == null ? "N/A" : String.format("%.1f", m.getAtt1()),
                m.getAtt2() == null ? "N/A" : String.format("%.1f", m.getAtt2()),
                m.getFinalExam() == null ? "N/A" : String.format("%.1f", m.getFinalExam()),
                m.getTotal(),
                m.getLetterGrade(),
                m.isPassed() ? "PASS" : "FAIL"
        ));

        System.out.println("══════════════════════════════════════════════════");
    }

    public void rateTeacher(Teacher teacher, double score) {
        if (!enrolledCourses.values().stream()
                .anyMatch(c -> c.getTeacherIds().contains(teacher.getUserID()))) {
            System.out.println("[STUDENT] You can only rate teachers of courses you're enrolled in.");
            return;
        }

        teacher.receiveRating(score);

        System.out.printf("[STUDENT] %s rated %s: %.1f/5%n",
                getName(), teacher.getName(), score);
    }

    public String getStudentId() { return studentId; }
    public double getGpa() { return gpa; }
    public int getYearOfStudy() { return yearOfStudy; }
    public String getMajor() { return major; }

    public Set<String> getCompletedCourseIds() {
        return Collections.unmodifiableSet(completedCourseIds);
    }

    public Map<String, Course> getEnrolledCourses() {
        return Collections.unmodifiableMap(enrolledCourses);
    }

    public void setYearOfStudy(int y) { this.yearOfStudy = y; }
    public void setMajor(String m) { this.major = m; }

    @Override
    public String toString() {
        return String.format("Student{name='%s', id='%s', major='%s', year=%d, gpa=%.2f}",
                getName(), studentId, major, yearOfStudy, gpa);
    }
}