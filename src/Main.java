import enums.LessonType;
import models.Course;
import models.Lesson;
import models.Mark;
import models.Schedule;
import observer.StudentNotificationService;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

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

        // 2. Credit registration check
        System.out.println("=== Credit Registration Check ===");
        List<Course> registered = new ArrayList<>();
        tryRegister(registered, oop);
        tryRegister(registered, math);
        tryRegister(registered, db);
        tryRegister(registered, english);
        tryRegister(registered, pe);

        Course heavyCourse = new Course("Advanced Physics", 5);
        tryRegister(registered, heavyCourse);

        // 3. Schedule
        System.out.println("\n=== Schedule ===");
        Schedule oopSchedule = new Schedule("CS101", "Object-Oriented Programming");
        oopSchedule.addLesson("Thursday 12:00",    new Lesson(LessonType.LECTURE,  "Room 461", " S.Pakita"));
        oopSchedule.addLesson("Thursday 13:00",    new Lesson(LessonType.LECTURE,  "Room 461", " M.Assubay"));
        oopSchedule.addLesson("Saturday 10:00", new Lesson(LessonType.PRACTICE, "Room 269",    " M.Assubay"));
        System.out.println(oopSchedule);

        // 4. Observer pattern — grade notifications
        System.out.println("=== Grade Notifications ===");
        Mark mark = new Mark("STU-001", "Object-Oriented Programming");
        mark.addObserver(new StudentNotificationService("Alice"));

        mark.setAtt1(28.0);
        mark.setAtt2(25.0);
        mark.setFinalMark(40.0);
    }

    private static void tryRegister(List<Course> registered, Course course) {
        if (Course.canRegister(registered, course)) {
            registered.add(course);
            int total = registered.stream().mapToInt(Course::getCredits).sum();
            System.out.printf("  [OK] %s — %d credits (total: %d/21)%n",
                    course.getName(), course.getCredits(), total);
        } else {
            int total = registered.stream().mapToInt(Course::getCredits).sum();
            System.out.printf("  [BLOCKED] %s — %d credits would exceed limit (current: %d/21)%n",
                    course.getName(), course.getCredits(), total);
        }
    }
}