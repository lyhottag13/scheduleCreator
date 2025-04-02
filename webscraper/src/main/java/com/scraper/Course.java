package com.scraper;

public record Course(String name, int ID, boolean isOnline, int[][] times) {
    public Course(Course course) {
        this(course.name(), course.ID(), course.isOnline(), course.times());
    }

    public String toString() {
        StringBuilder output = new StringBuilder();
        StringBuilder time = new StringBuilder();
//        for (int[] i : times) {
//            output.append(switch(i[0] / 10000) {
//                case 1:
//                    yield "Monday
//            }).append(":").append(i[1]).append(" ");
//        }
        for (int[] i : times) {
            output.append(switch (i[0] / 10000) {
                case 1:
                    yield " M,";
                case 2:
                    yield " Tu,";
                case 3:
                    yield " W,";
                case 4:
                    yield " Th,";
                case 5:
                    yield " F,";
                default:
                    throw new IllegalStateException("Unexpected value: " + i[0] / 10000);
            });
        }
        output.deleteCharAt(output.length() - 1);
        createCourseString(time);
        return name + " " + ID + " " + output + time;
    }

    private void createCourseString(StringBuilder output) {
        output.append(String.valueOf(times[0][0]).substring(3)).insert(0, (times[0][0] / 100 % 100 < 13) ? times[0][0] / 100 % 100: times[0][0] / 100 % 100 - 12).insert(0, " ");
        output.append("-").append((times[0][1] / 100 % 100 < 13) ? times[0][1] / 100 % 100: times[0][1] / 100 % 100 - 12).append(":").append(String.valueOf(times[0][1]).substring(3));
    }
}
