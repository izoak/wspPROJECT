import enums.LessonType;
import models.Course;
import models.Lesson;
import models.Mark;
import models.Schedule;
import observer.StudentNotificationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo / main entry point showing Person 2's components working together.
 *
 * Covers:
 *  - Course, Lesson, Mark, Schedule classes
 *  - LessonType enum
 *  - Business rules: max 21 credits, max 3 fails, Mark = att1 + att2 + final
 *  - Observer pattern: student gets notified when grades are posted
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("=== University System - Person 2 Demo ===\n");

        // 1. Create courses
        Course oop = new Course("Object-Oriented Programming", 5);
        Course math = new Course("Calculus", 6);
        Course db = new Course("Databases", 4);
        Course english = new Course("English", 3);
        Course pe = new Course("Physical Education", 2);

        oop.addTeacher("Dr. Smith");
        oop.addLesson(LessonType.LECTURE);
        oop.addLesson(LessonType.PRACTICE);

        math.addTeacher("Prof. Johnson");
        math.addLesson(LessonType.LECTURE);

        db.addTeacher("Dr. Lee");
        db.addLesson(LessonType.LECTURE);
        db.addLesson(LessonType.PRACTICE);

        System.out.println("Courses created:");
        System.out.println("  " + oop);
        System.out.println("  " + math);
        System.out.println("  " + db);
        System.out.println("  " + english);
        System.out.println("  " + pe);

        // 2. Test 21-credit rule
        System.out.println("\n=== Credit Registration Check ===");
        List<Course> registered = new ArrayList<>();

        tryRegister(registered, oop);   // 5 credits -> OK
        tryRegister(registered, math);  // +6 = 11 -> OK
        tryRegister(registered, db);    // +4 = 15 -> OK
        tryRegister(registered, english); // +3 = 18 -> OK

        // Adding PE (2 credits) -> 20 credits: OK
        tryRegister(registered, pe);

        // Try to add a heavy course that would push over 21
        Course heavyCourse = new Course("Advanced Physics", 5);
        tryRegister(registered, heavyCourse); // 20+5=25 -> BLOCKED

        // 3. Test 3-fail rule
        System.out.println("\n=== Fail Count Check ===");
        int fails = 2;
        System.out.println("Student has " + fails + " fails. Can register? " + Course.hasNotExceededFails(fails));
        fails = 3;
        System.out.println("Student has " + fails + " fails. Can register? " + Course.hasNotExceededFails(fails));

        // 4. Create lessons and schedule
        System.out.println("\n=== Schedule ===");
        Schedule oopSchedule = new Schedule("CS101", "Object-Oriented Programming");
        oopSchedule.addLesson("Monday 09:00", new Lesson(LessonType.LECTURE, "Room 101", "Dr. Smith"));
        oopSchedule.addLesson("Wednesday 11:00", new Lesson(LessonType.PRACTICE, "Lab 3", "Dr. Smith"));
        oopSchedule.addLesson("Friday 14:00", new Lesson(LessonType.LECTURE, "Room 101", "Dr. Smith"));
        System.out.println(oopSchedule);

        // 5. Observer pattern: Mark notifies student
        System.out.println("=== Observer Pattern: Grade Notifications ===");
        Mark studentMark = new Mark("STU-001", "Object-Oriented Programming");

        // Register student as observer
        StudentNotificationService notification = new StudentNotificationService("Alice");
        studentMark.addObserver(notification);

        System.out.println("\n[Teacher posts att1...]");
        studentMark.setAtt1(28.0); // out of 30

        System.out.println("\n[Teacher posts att2...]");
        studentMark.setAtt2(25.0); // out of 30

        System.out.println("\n[Teacher posts final exam...]");
        studentMark.setFinalMark(40.0); // out of 40

        System.out.println("\nFinal mark breakdown:");
        System.out.printf("  att1=%.1f + att2=%.1f + final=%.1f = TOTAL: %.1f%n",
                studentMark.getAtt1(), studentMark.getAtt2(),
                studentMark.getFinalMark(), studentMark.getTotal());
        System.out.println("  Passed: " + studentMark.isPassed());

        // Test a failing scenario
        System.out.println("\n[Testing a failing grade...]");
        Mark failingMark = new Mark("STU-002", "Calculus");
        failingMark.addObserver(new StudentNotificationService("Bob"));
        failingMark.setAtt1(10.0);
        failingMark.setAtt2(12.0);
        failingMark.setFinalMark(15.0);
        System.out.printf("  Total: %.1f | Passed: %s%n", failingMark.getTotal(), failingMark.isPassed());
    }

    private static void tryRegister(List<Course> registered, Course course) {
        if (Course.canRegister(registered, course)) {
            registered.add(course);
            int total = registered.stream().mapToInt(Course::getCredits).sum();
            System.out.printf("  [OK] Registered '%s' (%d credits). Total credits: %d/%d%n",
                    course.getName(), course.getCredits(), total, Course.MAX_CREDITS);
        } else {
            int total = registered.stream().mapToInt(Course::getCredits).sum();
            System.out.printf("  [BLOCKED] Cannot register '%s' (%d credits). Current: %d/%d — would exceed limit!%n",
                    course.getName(), course.getCredits(), total, Course.MAX_CREDITS);
        }
    }
}
