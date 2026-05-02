package university.models.users;

import university.database.DataStorage;
import university.enums.Gender;
import university.enums.ManagerType;
import university.models.academic.Course;
import university.models.academic.News;
import university.models.base.Employee;

import java.util.*;

public class Manager extends Employee {

    private static final long serialVersionUID = 1L;

    private ManagerType managerType;
    private final List<RegistrationRequest> pendingRequests = new ArrayList<>();

    public Manager(String name, String email, String password, Gender gender,
                   String employeeID, double salary, String department,
                   ManagerType managerType) {
        super(name, email, password, gender, employeeID, salary, department);
        this.managerType = managerType;
    }

    public void approveRegistration(RegistrationRequest request, Student student) {
        pendingRequests.remove(request);
        System.out.printf("[MANAGER] Approved: %s → %s%n",
                student.getName(), request.getCourseName());
    }

    public void rejectRegistration(RegistrationRequest request, String reason) {
        pendingRequests.remove(request);
        System.out.printf("[MANAGER] Rejected registration for %s: %s%n",
                request.getStudentName(), reason);
    }

    public void addPendingRequest(RegistrationRequest request) {
        pendingRequests.add(request);
    }

    public List<RegistrationRequest> getPendingRequests() {
        return Collections.unmodifiableList(pendingRequests);
    }

    public void assignCourseToTeacher(Course course, Teacher teacher) {
        course.addTeacher(teacher.getUserID());
        teacher.addCourse(course.getCourseId());
        System.out.printf("[MANAGER] Assigned %s to course '%s'%n",
                teacher.getName(), course.getName());
    }

    public void addCourse(Course course) {
        DataStorage.getInstance().addCourse(course);
        System.out.println("[MANAGER] Course added: " + course.getName());
    }

    public void manageNews(News news, boolean add) {
        DataStorage db = DataStorage.getInstance();
        if (add) {
            db.addNews(news);
            System.out.println("[MANAGER] News published: " + news.getTopic());
        } else {
            db.removeNews(news.getNewsId());
            System.out.println("[MANAGER] News removed: " + news.getTopic());
        }
    }

    public void createReports() {
        System.out.println("══════════════════════════════════════════════════");
        System.out.println("ACADEMIC PERFORMANCE REPORT");
        System.out.println("──────────────────────────────────────────────────");
        DataStorage.getInstance().getAllCourses().forEach(course -> {
            System.out.printf("%-25s | Avg: %5.1f | Pass: %d | Fail: %d%n",
                    course.getName(),
                    course.getAverageScore(),
                    course.getPassCount(),
                    course.getFailCount());
        });
        System.out.println("══════════════════════════════════════════════════");
    }

    public void viewStudentsSortedByGpa() {
        System.out.println("=== Students sorted by GPA ===");
        DataStorage.getInstance().getAllUsers().stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .sorted(Comparator.comparingDouble(Student::getGpa).reversed())
                .forEach(s -> System.out.printf("  %-25s GPA: %.2f%n", s.getName(), s.getGpa()));
    }

    public void viewStudentsSortedByName() {
        System.out.println("=== Students sorted alphabetically ===");
        DataStorage.getInstance().getAllUsers().stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .sorted(Comparator.comparing(Student::getName))
                .forEach(s -> System.out.printf("  %-25s GPA: %.2f%n", s.getName(), s.getGpa()));
    }

    public void viewTeachersSortedByRating() {
        System.out.println("=== Teachers sorted by rating ===");
        DataStorage.getInstance().getAllUsers().stream()
                .filter(u -> u instanceof Teacher)
                .map(u -> (Teacher) u)
                .sorted(Comparator.comparingDouble(Teacher::getRating).reversed())
                .forEach(t -> System.out.printf("  %-25s Rating: %.2f (%d votes)%n",
                        t.getName(), t.getRating(), t.getRatingCount()));
    }

    public void viewRequests() {
        System.out.println("=== Pending Registration Requests ===");
        if (pendingRequests.isEmpty()) {
            System.out.println("  No pending requests.");
        } else {
            pendingRequests.forEach(r -> System.out.println("  " + r));
        }
    }

    public ManagerType getManagerType() { return managerType; }
    public void setManagerType(ManagerType t) { this.managerType = t; }

    @Override
    public String toString() {
        return String.format("Manager{name='%s', type=%s, dept='%s'}",
                getName(), managerType, getDepartment());
    }

    public static class RegistrationRequest implements java.io.Serializable {

        private final String studentId;
        private final String studentName;
        private final String courseId;
        private final String courseName;

        public RegistrationRequest(String studentId, String studentName,
                                   String courseId, String courseName) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.courseId = courseId;
            this.courseName = courseName;
        }

        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getCourseId() { return courseId; }
        public String getCourseName() { return courseName; }

        @Override
        public String toString() {
            return String.format("Request[student='%s', course='%s']",
                    studentName, courseName);
        }
    }
}