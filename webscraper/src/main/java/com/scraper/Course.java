package com.scraper;

public record Course(String name, int ID, boolean isOnline, String times, String days) {
    public Course(Course course) {
        this(course.name(), course.ID(), course.isOnline(), course.times(), course.days());
    }

    public String toString() {
        return name + "\n    " + ID + "\n    " + times + "\n    " + days;
    }
}
