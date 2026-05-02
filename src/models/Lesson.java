package models;

import enums.LessonType;

/**
 * Lesson class from the UML diagram.
 * Attributes: +LessonType, +Room, +Teacher: String
 */
public class Lesson {

    // From diagram: +LessonType (enum)
    private LessonType lessonType;

    // From diagram: +Room
    private String room;

    // From diagram: +Teacher: String
    private String teacher;

    public Lesson(LessonType lessonType, String room, String teacher) {
        this.lessonType = lessonType;
        this.room = room;
        this.teacher = teacher;
    }

    public LessonType getLessonType() { return lessonType; }
    public void setLessonType(LessonType lessonType) { this.lessonType = lessonType; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    @Override
    public String toString() {
        return String.format("Lesson[type=%s, room=%s, teacher=%s]", lessonType, room, teacher);
    }
}
