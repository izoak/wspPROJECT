package models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Schedule class — links courses to their lessons by day/time.
 * Not explicitly in the UML diagram but required by Person 2's task spec.
 *
 * A schedule holds a map of time slots to lessons for a given course.
 */
public class Schedule {

    private String courseId;
    private String courseName;

    // Day+Time -> Lesson, e.g. "Monday 09:00" -> Lesson
    private Map<String, Lesson> timetable;

    public Schedule(String courseId, String courseName) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.timetable = new LinkedHashMap<>();
    }

    /**
     * Adds a lesson to the schedule at a specific day and time slot.
     * @param dayAndTime e.g. "Monday 09:00"
     * @param lesson     the Lesson to assign
     */
    public void addLesson(String dayAndTime, Lesson lesson) {
        timetable.put(dayAndTime, lesson);
    }

    /**
     * Removes a lesson from a specific slot.
     */
    public void removeLesson(String dayAndTime) {
        timetable.remove(dayAndTime);
    }

    /**
     * Returns all lessons for a given day.
     */
    public List<Lesson> getLessonsForDay(String day) {
        List<Lesson> result = new ArrayList<>();
        for (Map.Entry<String, Lesson> entry : timetable.entrySet()) {
            if (entry.getKey().startsWith(day)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    public Map<String, Lesson> getTimetable() { return timetable; }
    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Schedule for ").append(courseName).append(":\n");
        for (Map.Entry<String, Lesson> entry : timetable.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
