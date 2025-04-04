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
     * Checks to see if this contains an online course.
     *
     * @return a boolean stating whether this list has an online course in
     * it.
     */
    public boolean containsOnlineCourse() {
        for (Course c : this) {
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
