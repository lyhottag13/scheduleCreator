package com.scraper;

public record Course(String name, String ID, boolean isOnline, String times, String days, String instructor, String location) {
    public Course(Course course) {
        this(course.name(), course.ID(), course.isOnline(), course.times(), course.days(), course.instructor(), course.location());
    }

    public String toString() {
        return name + "\n    " + ID + "\n    " + times + "\n    " + days;
    }
}
