package models;

import enums.LessonType;

public class Lesson {

    private LessonType lessonType;

    private String room;

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
