package system;
import auth.AuthService;
import enums.LessonType;
import enums.ManagerType;
import enums.Position;
import exceptions.LowHIndexException;
import exceptions.NotResearcherException;
import logging.ActionLogger;
import models.Admin;
import models.BachelorStudent;
import models.Course;
import models.Manager;
import models.Mark;
import models.News;
import models.Student;
import models.Teacher;
import models.User;
import research.ConcreteResearcher;
import research.PaperByCitationsComparator;
import research.PaperByDateComparator;
import research.PaperByPagesComparator;
import research.ResearchPaper;
import research.ResearchProject;
import research.ResearcherDecorator;
import storage.UniversityRepository;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
public class UniversitySystem {
    private final AuthService authService;
    private final UniversityRepository repository;
    private final Map<String, Course> coursesById;
    private final Map<String, List<String>> pendingRegistrationsByStudentId;
    private final List<News> newsFeed;
    private final Map<String, ResearcherDecorator> researchersByUserId;
    private final Map<String, ResearchProject> projectsById;
    public UniversitySystem() {
        this.authService = new AuthService();
        this.repository = UniversityRepository.getInstance();
        this.coursesById = new LinkedHashMap<>();
        this.pendingRegistrationsByStudentId = new LinkedHashMap<>();
        this.newsFeed = new ArrayList<>();
        this.researchersByUserId = new LinkedHashMap<>();
        this.projectsById = new LinkedHashMap<>();
        seedUsers();
        seedCourses();
        seedAssignments();
        seedNews();
        seedResearch();
    }
    public Optional<User> login(String username, String password) {
        return authService.login(username, password);
    }
    public void logout(User user) {
        authService.logout(user);
    }
    public Optional<ResearcherDecorator> findResearcher(User user) {
        return Optional.ofNullable(researchersByUserId.get(user.getId()));
    }
    public boolean isResearcher(User user) {
        return researchersByUserId.containsKey(user.getId());
    }
    public List<ResearcherDecorator> getAllResearchers() {
        return new ArrayList<>(researchersByUserId.values());
    }
    public String promoteToResearcher(Manager manager, String username) {
        Optional<User> opt = repository.findByUsername(username);
        if (opt.isEmpty()) return "User not found.";
        User user = opt.get();
        if (researchersByUserId.containsKey(user.getId())) {
            return user.getFullName() + " is already a researcher.";
        }
        ConcreteResearcher researcher = new ConcreteResearcher(user);
        researchersByUserId.put(user.getId(), researcher);
        ActionLogger.log(manager.getUsername() + " promoted " + username + " to Researcher");
        return user.getFullName() + " is now a Researcher.";
    }
    public String assignSupervisor(Manager manager, String studentUsername, String supervisorUsername)
            throws LowHIndexException, NotResearcherException {
        Optional<User> studentOpt = repository.findByUsername(studentUsername);
        if (studentOpt.isEmpty()) return "Student not found.";
        if (!(studentOpt.get() instanceof BachelorStudent bs)) return "User is not a BachelorStudent.";
        if (bs.getYearOfStudy() != 4) return "Only 4th-year students need a research supervisor.";
        Optional<User> supOpt = repository.findByUsername(supervisorUsername);
        if (supOpt.isEmpty()) return "Supervisor not found.";
        User supUser = supOpt.get();
        ResearcherDecorator supResearcher = researchersByUserId.get(supUser.getId());
        if (supResearcher == null) {
            throw new NotResearcherException(supervisorUsername);
        }
        supResearcher.validateSupervisorEligibility();
        bs.setResearchSupervisorName(supUser.getFullName());
        repository.updateUser(bs);
        ActionLogger.log(manager.getUsername() + " assigned supervisor " + supervisorUsername + " to " + studentUsername);
        return "Supervisor " + supUser.getFullName() + " assigned to " + bs.getFullName() + ".";
    }
    public String addResearchPaper(ResearcherDecorator researcher, String paperId, String title,
                                    String journal, LocalDate date, int pages, int citations) {
        ResearchPaper paper = new ResearchPaper(paperId, title, journal, date);
        paper.addAuthor(researcher.getFullName());
        paper.setPages(pages);
        paper.setCitations(citations);
        researcher.addPaper(paper);
        ActionLogger.log(researcher.getUsername() + " added paper: " + title);
        return "Paper '" + title + "' added. New h-index: " + researcher.calculateHIndex();
    }
    public void printResearchPapersSorted(ResearcherDecorator researcher, String sortBy) {
        Comparator<ResearchPaper> comparator = switch (sortBy.toLowerCase()) {
            case "date"     -> new PaperByDateComparator();
            case "pages"    -> new PaperByPagesComparator();
            default         -> new PaperByCitationsComparator(); 
        };
        researcher.printPapers(comparator);
    }
    public void printAllResearchPapersSorted(String sortBy) {
        Comparator<ResearchPaper> comparator = switch (sortBy.toLowerCase()) {
            case "date"  -> new PaperByDateComparator();
            case "pages" -> new PaperByPagesComparator();
            default      -> new PaperByCitationsComparator();
        };
        List<ResearchPaper> allPapers = researchersByUserId.values().stream()
                .flatMap(r -> r.getPapers().stream())
                .distinct()
                .sorted(comparator)
                .toList();
        if (allPapers.isEmpty()) {
            System.out.println("No research papers in the system.");
            return;
        }
        System.out.println("── All Research Papers (" + sortBy + ") ─────────────────────");
        for (int i = 0; i < allPapers.size(); i++) {
            ResearchPaper p = allPapers.get(i);
            System.out.printf("  %2d. [%s] '%s' | %s | cit: %d | pages: %d%n",
                    i + 1, p.getPublicationDate(), p.getTitle(), p.getJournal(),
                    p.getCitations(), p.getPages());
        }
    }
    public String createResearchProject(Manager manager, String projectId, String topic) {
        if (projectsById.containsKey(projectId)) return "Project with this ID already exists.";
        ResearchProject project = new ResearchProject(projectId, topic);
        projectsById.put(projectId, project);
        ActionLogger.log(manager.getUsername() + " created project " + projectId + ": " + topic);
        return "Project '" + topic + "' created.";
    }
    public String joinResearchProject(ResearcherDecorator researcher, String projectId) {
        ResearchProject project = projectsById.get(projectId);
        if (project == null) return "Project not found.";
        try {
            researcher.joinProject(project);
            ActionLogger.log(researcher.getUsername() + " joined project " + projectId);
            return "Joined project: " + project.getTopic();
        } catch (NotResearcherException e) {
            return "Error: " + e.getMessage();
        }
    }
    public void listResearchProjects() {
        if (projectsById.isEmpty()) {
            System.out.println("No research projects found.");
            return;
        }
        System.out.println("── Research Projects ────────────────────────────────");
        projectsById.values().forEach(p -> System.out.println("  " + p));
    }
    public void printTopResearchers() {
        ResearcherDecorator.printTopResearchers(getAllResearchers(), 5);
    }
    public void printTopResearchers(int n) {
        ResearcherDecorator.printTopResearchers(getAllResearchers(), n);
    }
    public String createAcademicReport() {
        List<Student> students = getStudents();
        if (students.isEmpty()) return "No students found.";
        double averageGpa = students.stream().mapToDouble(Student::calculateGpa).average().orElse(0.0);
        long studentsWithFails = students.stream().filter(s -> s.getFailedCoursesCount() > 0).count();
        long passed = students.stream().filter(s -> s.getFailedCoursesCount() == 0 && !s.getMarksByCourseName().isEmpty()).count();
        OptionalDouble maxGpa = students.stream().mapToDouble(Student::calculateGpa).max();
        OptionalDouble minGpa = students.stream().mapToDouble(Student::calculateGpa).min();
        return "---------------------------------------" + System.lineSeparator()
                + "         ACADEMIC REPORT               " + System.lineSeparator()
                + "----------------------------------------" + System.lineSeparator()
                + "Total students:          " + students.size() + System.lineSeparator()
                + "Average GPA:             " + String.format("%.2f", averageGpa) + System.lineSeparator()
                + "Max GPA:                 " + String.format("%.2f", maxGpa.orElse(0)) + System.lineSeparator()
                + "Min GPA:                 " + String.format("%.2f", minGpa.orElse(0)) + System.lineSeparator()
                + "Students with no fails:  " + passed + System.lineSeparator()
                + "Students with fails:     " + studentsWithFails + System.lineSeparator()
                + "Total researchers:       " + researchersByUserId.size() + System.lineSeparator()
                + "Total research papers:   " + researchersByUserId.values().stream().mapToInt(r -> r.getPapers().size()).sum() + System.lineSeparator()
                + "Total projects:          " + projectsById.size() + System.lineSeparator()
                + "----------------------------------------";
    }
    public Student registerBachelorStudent(String firstName, String lastName, String username,
                                           String rawPassword, String major, int yearOfStudy) {
        String id = "S-" + (getStudents().size() + 100);
        BachelorStudent student = new BachelorStudent(
                id, firstName, lastName, username,
                AuthService.hashPassword(rawPassword), major, yearOfStudy);
        authService.register(student);
        return student;
    }
    public User createUserByRole(String role, String firstName, String lastName, String username,
                                  String rawPassword, String departmentOrMajor) {
        return createUserByRole(role, firstName, lastName, username, rawPassword, departmentOrMajor,
                null, null, 1);
    }
    public User createUserByRole(String role, String firstName, String lastName, String username,
                                  String rawPassword, String departmentOrMajor,
                                  String positionStr, String managerTypeStr, int yearOfStudy) {
        String normalizedRole = role.trim().toUpperCase();
        Position position = Position.LECTOR;
        if (positionStr != null && !positionStr.isBlank()) {
            try { position = Position.valueOf(positionStr.trim().toUpperCase()); }
            catch (IllegalArgumentException e) { throw new IllegalArgumentException(
                    "Invalid position. Use: LECTOR, SENIOR_LECTOR, PROFESSOR"); }
        }
        ManagerType managerType = ManagerType.DEPARTMENT;
        if (managerTypeStr != null && !managerTypeStr.isBlank()) {
            try { managerType = ManagerType.valueOf(managerTypeStr.trim().toUpperCase()); }
            catch (IllegalArgumentException e) { throw new IllegalArgumentException(
                    "Invalid manager type. Use: OR, DEPARTMENT, DEAN, RECTOR"); }
        }
        User user = switch (normalizedRole) {
            case "STUDENT" -> new BachelorStudent(
                    "S-" + (getStudents().size() + 100), firstName, lastName, username,
                    AuthService.hashPassword(rawPassword), departmentOrMajor, yearOfStudy);
            case "TEACHER" -> new Teacher(
                    "T-" + (getTeachers().size() + 100), firstName, lastName, username,
                    AuthService.hashPassword(rawPassword), departmentOrMajor, position);
            case "MANAGER" -> new Manager(
                    "M-" + (getManagers().size() + 100), firstName, lastName, username,
                    AuthService.hashPassword(rawPassword), departmentOrMajor, managerType);
            case "ADMIN" -> new Admin(
                    "A-" + (getAdmins().size() + 100), firstName, lastName, username,
                    AuthService.hashPassword(rawPassword), departmentOrMajor);
            default -> throw new IllegalArgumentException("Unsupported role. Use STUDENT, TEACHER, MANAGER or ADMIN.");
        };
        authService.register(user);
        if (user instanceof Teacher t && t.getPosition() == Position.PROFESSOR) {
            researchersByUserId.put(user.getId(), new ConcreteResearcher(user));
            ActionLogger.log("Auto-promoted professor " + username + " to Researcher");
        }
        return user;
    }
    public List<News> getNewsFeed() { return List.copyOf(newsFeed); }
    public List<Course> getAllCourses() { return new ArrayList<>(coursesById.values()); }
    public Map<String, ResearchProject> getProjectsById() { return Collections.unmodifiableMap(projectsById); }
    public List<Student> getStudents() {
        return repository.getAllUsers().stream()
                .filter(Student.class::isInstance).map(Student.class::cast)
                .sorted(Comparator.comparing(Student::getFullName)).collect(Collectors.toList());
    }
    public List<Teacher> getTeachers() {
        return repository.getAllUsers().stream()
                .filter(Teacher.class::isInstance).map(Teacher.class::cast)
                .sorted(Comparator.comparing(Teacher::getFullName)).collect(Collectors.toList());
    }
    public List<Manager> getManagers() {
        return repository.getAllUsers().stream()
                .filter(Manager.class::isInstance).map(Manager.class::cast)
                .sorted(Comparator.comparing(Manager::getFullName)).collect(Collectors.toList());
    }
    public List<Admin> getAdmins() {
        return repository.getAllUsers().stream()
                .filter(Admin.class::isInstance).map(Admin.class::cast)
                .sorted(Comparator.comparing(Admin::getFullName)).collect(Collectors.toList());
    }
    public Collection<User> getAllUsers() {
        return repository.getAllUsers().stream().sorted().collect(Collectors.toList());
    }
    public String requestCourseRegistration(Student student, String courseId) {
        Course course = coursesById.get(courseId);
        if (course == null) return "Course not found.";
        if (!student.canRegister(course)) {
            return "Registration rejected. Check credits limit (21), fails limit (3), or duplicate course.";
        }
        List<String> pending = pendingRegistrationsByStudentId.computeIfAbsent(student.getId(), k -> new ArrayList<>());
        if (pending.contains(courseId)) return "This course is already waiting for manager approval.";
        pending.add(courseId);
        ActionLogger.log(student.getUsername() + " requested registration for " + courseId);
        return "Registration request submitted for manager approval.";
    }
    public List<String> getPendingRegistrations() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : pendingRegistrationsByStudentId.entrySet()) {
            String studentName = repository.findById(entry.getKey()).map(User::getFullName).orElse(entry.getKey());
            for (String courseId : entry.getValue()) {
                Course course = coursesById.get(courseId);
                result.add(studentName + " -> " + courseId + " " + (course == null ? "" : course.getName()));
            }
        }
        return result;
    }
    public String approveRegistration(Manager manager, String studentUsername, String courseId) {
        Optional<Student> studentOpt = findStudentByUsername(studentUsername);
        if (studentOpt.isEmpty()) return "Student not found.";
        Course course = coursesById.get(courseId);
        if (course == null) return "Course not found.";
        Student student = studentOpt.get();
        List<String> pending = pendingRegistrationsByStudentId.get(student.getId());
        if (pending == null || !pending.remove(courseId)) return "No pending request for this student and course.";
        student.approveCourseRegistration(course);
        repository.updateUser(student);
        ActionLogger.log(manager.getUsername() + " approved " + courseId + " for " + student.getUsername());
        return "Registration approved.";
    }
    public String assignCourseToTeacher(Manager manager, String courseId, String teacherUsername) {
        Optional<Teacher> teacherOpt = findTeacherByUsername(teacherUsername);
        if (teacherOpt.isEmpty()) return "Teacher not found.";
        Course course = coursesById.get(courseId);
        if (course == null) return "Course not found.";
        Teacher teacher = teacherOpt.get();
        teacher.assignCourse(course);
        course.addTeacher(teacher.getFullName());
        repository.updateUser(teacher);
        ActionLogger.log(manager.getUsername() + " assigned " + courseId + " to " + teacher.getUsername());
        return "Course assigned to teacher.";
    }
    public String addNews(User author, String title, String content) {
        News news = new News("NEWS-" + (newsFeed.size() + 1), title, content, author.getFullName());
        newsFeed.add(news);
        ActionLogger.log(author.getUsername() + " published news " + news.getNewsId());
        return "News added.";
    }
    public String putMark(Teacher teacher, String studentUsername, String courseId,
                          double att1, double att2, double finalMark) {
        Optional<Student> studentOpt = findStudentByUsername(studentUsername);
        if (studentOpt.isEmpty()) return "Student not found.";
        Course course = coursesById.get(courseId);
        if (course == null) return "Course not found.";
        if (!teacher.teachesCourse(course)) return "This teacher is not assigned to the selected course.";
        Student student = studentOpt.get();
        Mark mark = student.getMark(course.getName()).orElseGet(() -> new Mark(student.getId(), course.getName()));
        mark.addObserver(student);
        mark.setAtt1(att1);
        mark.setAtt2(att2);
        mark.setFinalMark(finalMark);
        student.addOrUpdateMark(mark);
        repository.updateUser(student);
        ActionLogger.log(teacher.getUsername() + " updated marks for " + student.getUsername() + " in " + courseId);
        return "Mark saved. Total = " + String.format("%.1f", mark.getTotal());
    }
    public String rateTeacher(Student student, String teacherUsername, int rating) {
        Optional<Teacher> teacherOpt = findTeacherByUsername(teacherUsername);
        if (teacherOpt.isEmpty()) return "Teacher not found.";
        Teacher teacher = teacherOpt.get();
        teacher.addRating(rating);
        student.rateTeacher(teacherUsername, rating);
        repository.updateUser(student);
        repository.updateUser(teacher);
        ActionLogger.log(student.getUsername() + " rated " + teacherUsername + " with " + rating);
        return "Teacher rated successfully.";
    }
    public String setUserActiveStatus(String username, boolean active, String reason) {
        Optional<User> opt = repository.findByUsername(username);
        if (opt.isEmpty()) return "User not found.";
        User user = opt.get();
        user.setActive(active);
        if (!active && reason != null && !reason.isBlank()) {
            user.setDeactivationReason(reason);
        } else if (active) {
            user.setDeactivationReason(null);
        }
        repository.updateUser(user);
        ActionLogger.log("Admin changed active status for " + username + " to " + active
                + (!active && reason != null ? " | reason: " + reason : ""));
        return "User status updated.";
    }
    public String removeUser(String userId) {
        boolean removed = repository.removeUser(userId);
        return removed ? "User removed." : "User not found.";
    }
    public List<Student> getStudentsForCourse(String courseId) {
        Course course = coursesById.get(courseId);
        if (course == null) return List.of();
        return getStudents().stream()
                .filter(s -> s.getRegisteredCourses().contains(course))
                .collect(Collectors.toList());
    }
    private Optional<Student> findStudentByUsername(String username) {
        return repository.findByUsername(username).filter(Student.class::isInstance).map(Student.class::cast);
    }
    private Optional<Teacher> findTeacherByUsername(String username) {
        return repository.findByUsername(username).filter(Teacher.class::isInstance).map(Teacher.class::cast);
    }
    private void seedUsers() {
        ensureUser(new Admin("A-001", "Adiz", "Ashirbekov", "admin",
                AuthService.hashPassword("admin123"), "Administration"));
        ensureUser(new BachelorStudent("S-001", "Islam", "Meldebek", "student",
                AuthService.hashPassword("student123"), "Computer Science", 2));
        ensureUser(new Teacher("T-001", "Nurislam", "Syrgabayev", "teacher",
                AuthService.hashPassword("teacher123"), "FIT", Position.PROFESSOR));
        ensureUser(new Manager("M-001", "Akniyet", "Baltabek", "manager",
                AuthService.hashPassword("manager123"), "Registrar Office", ManagerType.OR));
    }
    private void seedCourses() {
        Course oop = new Course("CS101", "Object-Oriented Programming", 5);
        oop.addLesson(LessonType.LECTURE);
        oop.addLesson(LessonType.PRACTICE);
        Course db = new Course("CS202", "Databases", 4);
        db.addLesson(LessonType.LECTURE);
        db.addLesson(LessonType.PRACTICE);
        Course calc = new Course("MATH101", "Calculus", 6);
        calc.addLesson(LessonType.LECTURE);
        Course english = new Course("ENG201", "Academic English", 3);
        english.addLesson(LessonType.PRACTICE);
        coursesById.put(oop.getCourseId(), oop);
        coursesById.put(db.getCourseId(), db);
        coursesById.put(calc.getCourseId(), calc);
        coursesById.put(english.getCourseId(), english);
    }
    private void seedAssignments() {
        Optional<Teacher> teacherOpt = findTeacherByUsername("teacher");
        Optional<Manager> managerOpt = repository.findByUsername("manager")
                .filter(Manager.class::isInstance).map(Manager.class::cast);
        if (teacherOpt.isPresent() && managerOpt.isPresent()) {
            Teacher teacher = teacherOpt.get();
            Manager manager = managerOpt.get();
            if (teacher.getAssignedCourses().isEmpty()) {
                assignCourseToTeacher(manager, "CS101", "teacher");
                assignCourseToTeacher(manager, "CS202", "teacher");
            }
        }
    }
    private void seedNews() {
        if (newsFeed.isEmpty()) {
            newsFeed.add(new News("NEWS-1", "Welcome to WSP University",
                    "Console system is ready for login, registration and academic workflows.", "System"));
            newsFeed.add(new News("NEWS-2", "Course Registration Open",
                    "Students can submit registration requests and managers can approve them.", "Registrar Office"));
        }
    }
    private void seedResearch() {
        repository.findByUsername("teacher")
                .filter(Teacher.class::isInstance).map(Teacher.class::cast)
                .ifPresent(teacher -> {
                    if (!researchersByUserId.containsKey(teacher.getId())) {
                        ConcreteResearcher researcher = new ConcreteResearcher(teacher);
                        ResearchPaper p1 = new ResearchPaper("P-001", "Deep Learning in NLP",
                                "IEEE Transactions", LocalDate.of(2022, 3, 10));
                        p1.addAuthor(teacher.getFullName()); p1.setPages(14); p1.setCitations(45);
                        ResearchPaper p2 = new ResearchPaper("P-002", "Graph Neural Networks Survey",
                                "Nature Machine Intelligence", LocalDate.of(2023, 7, 1));
                        p2.addAuthor(teacher.getFullName()); p2.setPages(28); p2.setCitations(120);
                        ResearchPaper p3 = new ResearchPaper("P-003", "Federated Learning Privacy",
                                "ACM Computing Surveys", LocalDate.of(2024, 2, 20));
                        p3.addAuthor(teacher.getFullName()); p3.setPages(22); p3.setCitations(67);
                        researcher.addPaper(p1);
                        researcher.addPaper(p2);
                        researcher.addPaper(p3);
                        researchersByUserId.put(teacher.getId(), researcher);
                    }
                });
        if (projectsById.isEmpty()) {
            ResearchProject proj = new ResearchProject("RP-001", "AI in Education");
            projectsById.put(proj.getProjectId(), proj);
        }
    }
    private void ensureUser(User user) {
        if (repository.findById(user.getId()).isEmpty() && repository.findByUsername(user.getUsername()).isEmpty()) {
            authService.register(user);
        }
        if (user instanceof Teacher t && t.getPosition() == Position.PROFESSOR) {
            researchersByUserId.computeIfAbsent(user.getId(), k -> new ConcreteResearcher(user));
        }
    }
}