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
     * @param list a list of courses.
     * @return a boolean stating whether or not this list has an online course in it.
     */
    public static boolean containsOnlineCourse(ListOfCourses<Course> list) {
        for (Course c : list) {
            if (c.isOnline()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if the entire list of courses has no overlap.
     * 
     * @param list the list of courses.
     * @return a boolean representation of the list's validity.
     */
    public static boolean isValidList(ListOfCourses<Course> list, int[] constraints) {
        Course course;
        for (int i = 0; i < list.size(); i++) {
            course = list.get(i);
            for (int j = 0; j < i; j++) {
                if (!isCompatible(course, list.get(j))) {
                    return false;
                }
            }
        }
        for (Course c : list) {
            if (!isWithinTimeConstraints(c, constraints)) {
                App.totalPossibleSchedules++;
                return false;
            }
        }
        App.totalPossibleSchedules++;
        return true;
    }

    /**
     * Checks to see if the timeslot is within the time constraints.
     * 
     * @param course      a course's timeslot.
     * @param constraints the time constraints set by the user.
     * @return a boolean stating whether or not this course's timeslots was within
     *         the time constraints.
     */
    public static boolean isWithinTimeConstraints(Course course, int[] constraints) {
        int courseBottom = course.times()[0][0] % 10000;
        int courseTop = course.times()[0][1] % 10000;
        return (courseBottom >= constraints[0] && courseTop <= constraints[1]);
    }

    /**
     * Checks to see if the times of two timeslots are compatible.
     * 
     * @param times1 the start and end times of the first course.
     * @param times2 the start and end times of the second course.
     * @return a boolean representation of the courses' compatibility.
     */
    public static boolean isCompatible(Course course1, Course course2) {
        int[][] times1 = course1.times();
        int[][] times2 = course2.times();
        for (int[] row1 : times1) {
            for (int i1 : row1) {
                for (int[] row2 : times2) {
                    if (i1 > row2[0] && i1 < row2[1])
                        return false;
                }
            }
        }
        for (int[] row2 : times2) {
            for (int i2 : row2) {
                for (int[] row1 : times1) {
                    if (i2 > row1[0] && i2 < row1[1])
                        return false;
                }
            }
        }
        return true;
    }

    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            output.append(get(i) + "\n");
        }
        return output.toString();
    }
}
