package com.scraper;

public record Course(String name, int ID, boolean isOnline, int[][] times) {
    public Course(String name, int ID, boolean isOnline, int[][] times) {
        this.name = name;
        this.ID = ID;
        this.isOnline = isOnline;
        this.times = times;
    }
    public Course(Course course) {
        this(course.name(), course.ID(), course.isOnline(), course.times());
    }
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int[] i : times) {
            output.append(i[0] + ":" + i[1] + " ");
        }
        return name + " " + ID + " " + output;
    }
}
