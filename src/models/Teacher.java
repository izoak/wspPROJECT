package university.models.users;

import university.enums.Gender;
import university.enums.Position;
import university.models.academic.Course;
import university.models.academic.Mark;
import university.models.base.Employee;
import university.models.research.Researcher;
import university.models.research.ResearchPaper;
import university.models.research.ResearchProject;
import university.patterns.decorator.ResearcherDecorator;

import java.util.*;

public class Teacher extends Employee implements Researcher {

    private static final long serialVersionUID = 1L;

    private Position position;
    private double rating;
    private int ratingCount;
    private final List<String> courseIds = new ArrayList<>();

    private ResearcherDecorator researcherDecorator;

    public Teacher(String name, String email, String password, Gender gender,
                   String employeeID, double salary, String department,
                   Position position) {
        super(name, email, password, gender, employeeID, salary, department);
        this.position = position;
        this.rating = 0.0;
        this.ratingCount = 0;

        if (position.isAlwaysResearcher()) {
            this.researcherDecorator = new ResearcherDecorator(this, getUserID(), name);
        }
    }

    public void putAtt1(Course course, String studentId, double score) {
        course.setAtt1(studentId, score);
        System.out.printf("[TEACHER:%s] Set ATT1=%.2f for student %s in %s%n",
                getName(), score, studentId, course.getName());
    }

    public void putAtt2(Course course, String studentId, double score) {
        course.setAtt2(studentId, score);
        System.out.printf("[TEACHER:%s] Set ATT2=%.2f for student %s in %s%n",
                getName(), score, studentId, course.getName());
    }

    public void putFinal(Course course, String studentId, double score) {
        course.setFinalExam(studentId, score);
        System.out.printf("[TEACHER:%s] Set FINAL=%.2f for student %s in %s%n",
                getName(), score, studentId, course.getName());
    }

    public void viewStudentMarks(Course course) {
        System.out.println("=== Marks for " + course.getName() + " ===");
        course.getAllMarks().forEach(System.out::println);
    }

    public void viewCourseReport(Course course) {
        course.printReport();
    }

    public void receiveRating(double score) {
        if (score < 1 || score > 5)
            throw new IllegalArgumentException("Rating must be 1–5.");

        rating = (rating * ratingCount + score) / (ratingCount + 1);
        ratingCount++;
    }

    public boolean isResearcher() {
        return researcherDecorator != null;
    }

    public void enableResearcher() {
        if (researcherDecorator == null) {
            researcherDecorator = new ResearcherDecorator(this, getUserID(), getName());
        }
    }

    @Override
    public int getHIndex() {
        ensureResearcher();
        return researcherDecorator.getHIndex();
    }

    @Override
    public List<ResearchPaper> getResearchPapers() {
        ensureResearcher();
        return researcherDecorator.getResearchPapers();
    }

    @Override
    public void addResearchPaper(ResearchPaper paper) {
        ensureResearcher();
        researcherDecorator.addResearchPaper(paper);
    }

    @Override
    public List<ResearchProject> getResearchProjects() {
        ensureResearcher();
        return researcherDecorator.getResearchProjects();
    }

    @Override
    public void printPapers(Comparator<ResearchPaper> comparator) {
        ensureResearcher();
        researcherDecorator.printPapers(comparator);
    }

    @Override
    public void calculateHIndex() {
        ensureResearcher();
        researcherDecorator.calculateHIndex();
    }

    private void ensureResearcher() {
        if (researcherDecorator == null) {
            throw new IllegalStateException(getName() + " is not a researcher.");
        }
    }

    public Position getPosition() { return position; }
    public double getRating() { return rating; }
    public int getRatingCount() { return ratingCount; }

    public List<String> getCourseIds() {
        return Collections.unmodifiableList(courseIds);
    }

    public void setPosition(Position p) { this.position = p; }
    public void addCourse(String courseId) { courseIds.add(courseId); }
    public void removeCourse(String id) { courseIds.remove(id); }

    @Override
    public String toString() {
        return String.format("Teacher{name='%s', position=%s, rating=%.2f, courses=%d}",
                getName(), position, rating, courseIds.size());
    }
}