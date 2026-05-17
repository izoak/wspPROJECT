import exceptions.LowHIndexException;
import exceptions.NotResearcherException;
import models.Admin;
import models.BachelorStudent;
import models.Course;
import models.Manager;
import models.News;
import models.Student;
import models.Teacher;
import models.User;
import research.ResearcherDecorator;
import system.UniversitySystem;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        UniversitySystem system = new UniversitySystem();
        Scanner scanner = new Scanner(System.in);
        printWelcome();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt(scanner);
            switch (choice) {
                case 1 -> handleLogin(system, scanner);
                case 2 -> handleStudentRegistration(system, scanner);
                case 3 -> printNews(system.getNewsFeed());
                case 4 -> printCourses(system.getAllCourses());
                case 5 -> system.printTopResearchers();
                case 0 -> {
                    System.out.println("Goodbye.");
                    running = false;
                }
                default -> System.out.println("Unknown option. Try again.");
            }
        }
    }
    private static void handleLogin(UniversitySystem system, Scanner scanner) {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        Optional<User> userOptional = system.login(username, password);
        if (userOptional.isEmpty()) {
            System.out.println("Login failed.");
            return;
        }
        User user = userOptional.get();
        boolean isResearcher = system.isResearcher(user);
        System.out.println("Logged in as " + user.getFullName() + " [" + user.getRole() + "]"
                + (isResearcher ? " [RESEARCHER]" : ""));
        try {
            if (user instanceof Student student) {
                studentMenu(system, student, scanner, isResearcher);
            } else if (user instanceof Teacher teacher) {
                teacherMenu(system, teacher, scanner, isResearcher);
            } else if (user instanceof Manager manager) {
                managerMenu(system, manager, scanner, isResearcher);
            } else if (user instanceof Admin admin) {
                adminMenu(system, admin, scanner);
            } else {
                System.out.println("No menu defined for this role.");
            }
        } finally {
            system.logout(user);
            System.out.println("Logged out.");
        }
    }
    private static void studentMenu(UniversitySystem system, Student student, Scanner scanner, boolean isResearcher) {
        boolean inside = true;
        while (inside) {
            System.out.println();
            System.out.println("=== Student Menu ===");
            System.out.println("1.  View news");
            System.out.println("2.  View available courses");
            System.out.println("3.  Register for course");
            System.out.println("4.  View registered courses");
            System.out.println("5.  View marks");
            System.out.println("6.  Get transcript");
            System.out.println("7.  Rate teacher");
            System.out.println("8.  View notifications");
            System.out.println("9.  Show top researchers");
            if (isResearcher) {
                System.out.println("── Researcher Options ──────────────────");
                System.out.println("10. My research papers");
                System.out.println("11. Add research paper");
                System.out.println("12. View all university papers");
                System.out.println("13. View research projects");
                System.out.println("14. Join research project");
            }
            System.out.println("0.  Logout");
            System.out.print("Choose: ");
            int choice = readInt(scanner);
            switch (choice) {
                case 1 -> printNews(system.getNewsFeed());
                case 2 -> printCourses(system.getAllCourses());
                case 3 -> {
                    System.out.print("Enter course ID: ");
                    System.out.println(system.requestCourseRegistration(student, scanner.nextLine().trim()));
                }
                case 4 -> printCourses(student.getRegisteredCourses());
                case 5 -> {
                    if (student.getMarksByCourseName().isEmpty()) System.out.println("No marks yet.");
                    else student.getMarksByCourseName().values().forEach(System.out::println);
                }
                case 6 -> System.out.println(student.getTranscript());
                case 7 -> {
                    System.out.print("Teacher username: ");
                    String teacherUsername = scanner.nextLine().trim();
                    System.out.print("Rating (1-5): ");
                    int rating = readInt(scanner);
                    try { System.out.println(system.rateTeacher(student, teacherUsername, rating)); }
                    catch (IllegalArgumentException e) { System.out.println(e.getMessage()); }
                }
                case 8 -> {
                    if (student.getNotifications().isEmpty()) System.out.println("No notifications yet.");
                    else student.getNotifications().forEach(System.out::println);
                }
                case 9 -> system.printTopResearchers();
                case 10 -> {
                    if (!isResearcher) { System.out.println("Unknown option."); break; }
                    researcherPapersMenu(system, student, scanner);
                }
                case 11 -> {
                    if (!isResearcher) { System.out.println("Unknown option."); break; }
                    handleAddPaper(system, student, scanner);
                }
                case 12 -> {
                    if (!isResearcher) { System.out.println("Unknown option."); break; }
                    handlePrintAllPapers(system, scanner);
                }
                case 13 -> {
                    if (!isResearcher) { System.out.println("Unknown option."); break; }
                    system.listResearchProjects();
                }
                case 14 -> {
                    if (!isResearcher) { System.out.println("Unknown option."); break; }
                    handleJoinProject(system, student, scanner);
                }
                case 0 -> inside = false;
                default -> System.out.println("Unknown option.");
            }
        }
    }
    private static void teacherMenu(UniversitySystem system, Teacher teacher, Scanner scanner, boolean isResearcher) {
        boolean inside = true;
        while (inside) {
            System.out.println();
            System.out.println("=== Teacher Menu ===");
            System.out.println("1. View assigned courses");
            System.out.println("2. View students for course");
            System.out.println("3. Put mark");
            System.out.println("4. View inbox");
            System.out.println("5. Show top researchers");
            if (isResearcher) {
                System.out.println("── Researcher Options ──────────────────");
                System.out.println("6.  My research papers");
                System.out.println("7.  Add research paper");
                System.out.println("8.  View all university papers");
                System.out.println("9.  View research projects");
                System.out.println("10. Join research project");
            }
            System.out.println("0. Logout");
            System.out.print("Choose: ");
            int choice = readInt(scanner);
            switch (choice) {
                case 1 -> {
                    printCourses(teacher.getAssignedCourses());
                    System.out.printf("Average rating: %.2f%n", teacher.getAverageRating());
                }
                case 2 -> {
                    System.out.print("Enter course ID: ");
                    String courseId = scanner.nextLine().trim();
                    List<Student> students = system.getStudentsForCourse(courseId);
                    if (students.isEmpty()) System.out.println("No students found for this course.");
                    else students.forEach(s -> System.out.println(s.getUsername() + " - " + s.getFullName()));
                }
                case 3 -> {
                    System.out.print("Student username: ");
                    String studentUsername = scanner.nextLine().trim();
                    System.out.print("Course ID: ");
                    String courseId = scanner.nextLine().trim();
                    System.out.print("Attestation 1 (0-30): ");
                    double att1 = readDouble(scanner);
                    System.out.print("Attestation 2 (0-30): ");
                    double att2 = readDouble(scanner);
                    System.out.print("Final exam (0-40): ");
                    double finalExam = readDouble(scanner);
                    try { System.out.println(system.putMark(teacher, studentUsername, courseId, att1, att2, finalExam)); }
                    catch (IllegalArgumentException e) { System.out.println(e.getMessage()); }
                }
                case 4 -> {
                    if (teacher.getInbox().isEmpty()) System.out.println("Inbox is empty.");
                    else teacher.getInbox().forEach(System.out::println);
                }
                case 5 -> system.printTopResearchers();
                case 6 -> {
                    if (!isResearcher) { System.out.println("Unknown option."); break; }
                    researcherPapersMenu(system, teacher, scanner);
                }
                case 7 -> {
                    if (!isResearcher) { System.out.println("Unknown option."); break; }
                    handleAddPaper(system, teacher, scanner);
                }
                case 8 -> {
                    if (!isResearcher) { System.out.println("Unknown option."); break; }
                    handlePrintAllPapers(system, scanner);
                }
                case 9 -> {
                    if (!isResearcher) { System.out.println("Unknown option."); break; }
                    system.listResearchProjects();
                }
                case 10 -> {
                    if (!isResearcher) { System.out.println("Unknown option."); break; }
                    handleJoinProject(system, teacher, scanner);
                }
                case 0 -> inside = false;
                default -> System.out.println("Unknown option.");
            }
        }
    }
    private static void managerMenu(UniversitySystem system, Manager manager, Scanner scanner, boolean isResearcher) {
        boolean inside = true;
        while (inside) {
            System.out.println();
            System.out.println("=== Manager Menu ===");
            System.out.println("1.  View pending registrations");
            System.out.println("2.  Approve registration");
            System.out.println("3.  Assign course to teacher");
            System.out.println("4.  Add news");
            System.out.println("5.  View students");
            System.out.println("6.  View teachers");
            System.out.println("7.  Create academic report");
            System.out.println("8.  View inbox");
            System.out.println("── Research Management ─────────────────");
            System.out.println("9.  Promote user to researcher");
            System.out.println("10. Assign supervisor to 4th-year student");
            System.out.println("11. Create research project");
            System.out.println("12. View all researchers");
            System.out.println("13. View all research projects");
            System.out.println("14. View all research papers");
            System.out.println("15. Show top researchers");
            if (isResearcher) {
                System.out.println("── My Researcher Options ───────────────");
                System.out.println("16. My research papers");
                System.out.println("17. Add research paper");
                System.out.println("18. Join research project");
            }
            System.out.println("0.  Logout");
            System.out.print("Choose: ");
            int choice = readInt(scanner);
            switch (choice) {
                case 1 -> {
                    List<String> pending = system.getPendingRegistrations();
                    if (pending.isEmpty()) System.out.println("No pending registrations.");
                    else pending.forEach(System.out::println);
                }
                case 2 -> {
                    System.out.print("Student username: ");
                    String su = scanner.nextLine().trim();
                    System.out.print("Course ID: ");
                    String ci = scanner.nextLine().trim();
                    System.out.println(system.approveRegistration(manager, su, ci));
                }
                case 3 -> {
                    System.out.print("Course ID: ");
                    String ci = scanner.nextLine().trim();
                    System.out.print("Teacher username: ");
                    String tu = scanner.nextLine().trim();
                    System.out.println(system.assignCourseToTeacher(manager, ci, tu));
                }
                case 4 -> {
                    System.out.print("News title: ");
                    String title = scanner.nextLine().trim();
                    System.out.print("News content: ");
                    String content = scanner.nextLine().trim();
                    System.out.println(system.addNews(manager, title, content));
                }
                case 5 -> system.getStudents().forEach(s ->
                        System.out.printf("%s | %s | GPA %.2f | credits %d%n",
                                s.getUsername(), s.getFullName(), s.calculateGpa(), s.getTotalCredits()));
                case 6 -> system.getTeachers().forEach(t ->
                        System.out.printf("%s | %s | %s | rating %.2f%n",
                                t.getUsername(), t.getFullName(), t.getPosition(), t.getAverageRating()));
                case 7 -> System.out.println(system.createAcademicReport());
                case 8 -> {
                    if (manager.getInbox().isEmpty()) System.out.println("Inbox is empty.");
                    else manager.getInbox().forEach(System.out::println);
                }
                case 9 -> {
                    System.out.print("Username to promote to researcher: ");
                    String u = scanner.nextLine().trim();
                    System.out.println(system.promoteToResearcher(manager, u));
                }
                case 10 -> {
                    System.out.print("4th-year student username: ");
                    String su = scanner.nextLine().trim();
                    System.out.print("Supervisor username: ");
                    String supU = scanner.nextLine().trim();
                    try {
                        System.out.println(system.assignSupervisor(manager, su, supU));
                    } catch (LowHIndexException e) {
                        System.out.println("ERROR: " + e.getMessage());
                    } catch (NotResearcherException e) {
                        System.out.println("ERROR: " + e.getMessage());
                    }
                }
                case 11 -> {
                    System.out.print("Project ID: ");
                    String pid = scanner.nextLine().trim();
                    System.out.print("Project topic: ");
                    String topic = scanner.nextLine().trim();
                    System.out.println(system.createResearchProject(manager, pid, topic));
                }
                case 12 -> {
                    List<ResearcherDecorator> researchers = system.getAllResearchers();
                    if (researchers.isEmpty()) System.out.println("No researchers yet.");
                    else researchers.forEach(r -> System.out.printf("  %s | h-index: %d | papers: %d%n",
                            r.getFullName(), r.getHIndex(), r.getPapers().size()));
                }
                case 13 -> system.listResearchProjects();
                case 14 -> handlePrintAllPapers(system, scanner);
                case 15 -> {
                    System.out.print("How many top researchers to show? (e.g. 5): ");
                    int n = readInt(scanner);
                    system.printTopResearchers(n);
                }
                case 16 -> {
                    if (!isResearcher) { System.out.println("Unknown option."); break; }
                    researcherPapersMenu(system, manager, scanner);
                }
                case 17 -> {
                    if (!isResearcher) { System.out.println("Unknown option."); break; }
                    handleAddPaper(system, manager, scanner);
                }
                case 18 -> {
                    if (!isResearcher) { System.out.println("Unknown option."); break; }
                    handleJoinProject(system, manager, scanner);
                }
                case 0 -> inside = false;
                default -> System.out.println("Unknown option.");
            }
        }
    }
    private static void adminMenu(UniversitySystem system, Admin admin, Scanner scanner) {
        boolean inside = true;
        while (inside) {
            System.out.println();
            System.out.println("=== Admin Menu ===");
            System.out.println("1. View users");
            System.out.println("2. Add user");
            System.out.println("3. Remove user");
            System.out.println("4. Activate or deactivate user");
            System.out.println("5. View logs");
            System.out.println("0. Logout");
            System.out.print("Choose: ");
            switch (readInt(scanner)) {
                case 1 -> system.getAllUsers().forEach(System.out::println);
                case 2 -> handleAdminUserCreation(system, scanner);
                case 3 -> {
                    System.out.print("User ID: ");
                    System.out.println(system.removeUser(scanner.nextLine().trim()));
                }
                case 4 -> {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Set active? (true/false): ");
                    boolean active = Boolean.parseBoolean(scanner.nextLine().trim());
                    String reason = "";
                    if (!active) {
                        System.out.print("Reason for deactivation: ");
                        reason = scanner.nextLine().trim();
                    }
                    System.out.println(system.setUserActiveStatus(username, active, reason));
                }
                case 5 -> {
                    if (admin.viewLogs().isEmpty()) System.out.println("No logs yet.");
                    else admin.viewLogs().forEach(System.out::println);
                }
                case 0 -> inside = false;
                default -> System.out.println("Unknown option.");
            }
        }
    }
    private static void researcherPapersMenu(UniversitySystem system, models.User user, Scanner scanner) {
        Optional<ResearcherDecorator> resOpt = system.findResearcher(user);
        if (resOpt.isEmpty()) { System.out.println("You are not a researcher."); return; }
        ResearcherDecorator researcher = resOpt.get();
        System.out.println("Sort by: 1. Citations  2. Date  3. Pages");
        System.out.print("Choose: ");
        int s = readInt(scanner);
        String sortBy = switch (s) {
            case 2 -> "date";
            case 3 -> "pages";
            default -> "citations";
        };
        system.printResearchPapersSorted(researcher, sortBy);
    }
    private static void handleAddPaper(UniversitySystem system, models.User user, Scanner scanner) {
        Optional<ResearcherDecorator> resOpt = system.findResearcher(user);
        if (resOpt.isEmpty()) { System.out.println("You are not a researcher."); return; }
        ResearcherDecorator researcher = resOpt.get();
        System.out.print("Paper ID (e.g. P-010): ");
        String pid = scanner.nextLine().trim();
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Journal: ");
        String journal = scanner.nextLine().trim();
        System.out.print("Publication date (yyyy-MM-dd): ");
        LocalDate date;
        try { date = LocalDate.parse(scanner.nextLine().trim()); }
        catch (DateTimeParseException e) { System.out.println("Invalid date format."); return; }
        System.out.print("Pages: ");
        int pages = readInt(scanner);
        System.out.print("Citations: ");
        int citations = readInt(scanner);
        System.out.println(system.addResearchPaper(researcher, pid, title, journal, date, pages, citations));
    }
    private static void handlePrintAllPapers(UniversitySystem system, Scanner scanner) {
        System.out.println("Sort by: 1. Citations  2. Date  3. Pages");
        System.out.print("Choose: ");
        int s = readInt(scanner);
        String sortBy = switch (s) { case 2 -> "date"; case 3 -> "pages"; default -> "citations"; };
        system.printAllResearchPapersSorted(sortBy);
    }
    private static void handleJoinProject(UniversitySystem system, models.User user, Scanner scanner) {
        Optional<ResearcherDecorator> resOpt = system.findResearcher(user);
        if (resOpt.isEmpty()) { System.out.println("You are not a researcher."); return; }
        system.listResearchProjects();
        System.out.print("Enter project ID to join: ");
        String pid = scanner.nextLine().trim();
        System.out.println(system.joinResearchProject(resOpt.get(), pid));
    }
    private static void handleStudentRegistration(UniversitySystem system, Scanner scanner) {
        System.out.print("First name: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("Last name: ");
        String lastName = scanner.nextLine().trim();
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Major: ");
        String major = scanner.nextLine().trim();
        System.out.print("Year of study: ");
        int year = readInt(scanner);
        try {
            Student student = system.registerBachelorStudent(firstName, lastName, username, password, major, year);
            System.out.println("Student registered: " + student.getUsername());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void handleAdminUserCreation(UniversitySystem system, Scanner scanner) {
        System.out.print("Role (STUDENT/TEACHER/MANAGER/ADMIN): ");
        String role = scanner.nextLine().trim().toUpperCase();
        System.out.print("First name: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("Last name: ");
        String lastName = scanner.nextLine().trim();
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print(role.equals("STUDENT") ? "Major: " : "Department: ");
        String departmentOrMajor = scanner.nextLine().trim();
        String positionStr = null;
        String managerTypeStr = null;
        int yearOfStudy = 1;
        if (role.equals("TEACHER")) {
            System.out.println("Position: 1. LECTOR  2. SENIOR_LECTOR  3. PROFESSOR");
            System.out.print("Choose: ");
            positionStr = switch (readInt(scanner)) {
                case 2 -> "SENIOR_LECTOR";
                case 3 -> "PROFESSOR";
                default -> "LECTOR";
            };
            System.out.println("Position set to: " + positionStr);
            if (positionStr.equals("PROFESSOR")) {
                System.out.println("Note: Professor will be automatically added as a Researcher.");
            }
        } else if (role.equals("MANAGER")) {
            System.out.println("Manager type: 1. OR  2. DEPARTMENT  3. DEAN  4. RECTOR");
            System.out.print("Choose: ");
            managerTypeStr = switch (readInt(scanner)) {
                case 1 -> "OR";
                case 3 -> "DEAN";
                case 4 -> "RECTOR";
                default -> "DEPARTMENT";
            };
            System.out.println("Manager type set to: " + managerTypeStr);
        } else if (role.equals("STUDENT")) {
            System.out.print("Year of study (1-4): ");
            yearOfStudy = readInt(scanner);
            if (yearOfStudy == 4) {
                System.out.println("Note: 4th-year student. Manager can assign a research supervisor later.");
            }
        }
        try {
            models.User user = system.createUserByRole(role, firstName, lastName, username, password,
                    departmentOrMajor, positionStr, managerTypeStr, yearOfStudy);
            System.out.println("User created: " + user.getUsername() + " [" + user.getRole() + "]");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void printNews(List<News> newsFeed) {
        System.out.println("=== News ===");
        if (newsFeed.isEmpty()) { System.out.println("No news available."); return; }
        newsFeed.forEach(news -> { System.out.println(news); System.out.println(); });
    }
    private static void printCourses(List<Course> courses) {
        if (courses.isEmpty()) { System.out.println("No courses available."); return; }
        courses.forEach(System.out::println);
    }
    private static void printMainMenu() {
        System.out.println();
        System.out.println("=== Main Menu ===");
        System.out.println("1. Login");
        System.out.println("2. Register as student");
        System.out.println("3. View news");
        System.out.println("4. View courses");
        System.out.println("5. Show top researchers");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }
    private static void printWelcome() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║      WSP University Console System   ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║  Demo accounts:                      ║");
        System.out.println("║  admin    / admin123                 ║");
        System.out.println("║  student  / student123               ║");
        System.out.println("║  teacher  / teacher123               ║");
        System.out.println("║  manager  / manager123               ║");
        System.out.println("╚══════════════════════════════════════╝");
    }
    private static int readInt(Scanner scanner) {
        while (true) {
            String line = scanner.nextLine().trim();
            try { return Integer.parseInt(line); }
            catch (NumberFormatException e) { System.out.print("Enter a valid number: "); }
        }
    }
    private static double readDouble(Scanner scanner) {
        while (true) {
            String line = scanner.nextLine().trim();
            try { return Double.parseDouble(line); }
            catch (NumberFormatException e) { System.out.print("Enter a valid decimal number: "); }
        }
    }
}