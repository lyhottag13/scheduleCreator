package com.scraper;

public record Course(int ID, boolean isOnline, int[] times) {
    public Course(int ID, boolean isOnline, int[] times) {
        this.ID = ID;
        this.isOnline = isOnline;
        this.times = times;
    }
    public Course(Course course) {
        this(course.ID(), course.isOnline(), course.times());
    }
    public String toString() {
        return ID + " " + isOnline + " " + times[0] + ":" + times[1];
    }
}
