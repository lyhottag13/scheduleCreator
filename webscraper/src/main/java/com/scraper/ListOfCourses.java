package com.scraper;

import java.util.LinkedList;

public class ListOfCourses<E> extends LinkedList<Course> {
    public ListOfCourses(ListOfCourses<E> list) {
        for (Course c : list) {
            add(new Course(c));
        }
    }

    public ListOfCourses() {
    }

    /**
     * Checks to see if the list passed into it contains an online course.
     *
     * @param list a list of courses.
     * @return a boolean stating whether this list has an online course in
     * it.
     */
    public static boolean containsOnlineCourse(ListOfCourses<Course> list) {
        for (Course c : list) {
            if (c.isOnline()) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder output = new StringBuilder();
        for (Course course : this) {
            output.append(course).append("\n");
        }
        return output.toString();
    }
}
