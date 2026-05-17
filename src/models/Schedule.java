package models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Schedule {

    private String courseId;
    private String courseName;

    private Map<String, Lesson> timetable;

    public Schedule(String courseId, String courseName) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.timetable = new LinkedHashMap<>();
    }

 
    public void addLesson(String dayAndTime, Lesson lesson) {
        timetable.put(dayAndTime, lesson);
    }

    public void removeLesson(String dayAndTime) {
        timetable.remove(dayAndTime);
    }

 
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
