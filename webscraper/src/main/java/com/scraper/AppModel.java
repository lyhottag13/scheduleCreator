package com.scraper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;

public class AppModel {
    enum Constants {
        PECOS,
        WILLIAMS,
        FALL,
        SPRING,
        SUMMER
    }

    private static Constants semesterSelection;
    private static Constants campusSelection;
    private static ArrayList<Course>[] classSchedules;
    private static ArrayList<ListOfCourses<Course>> validSchedules;
    private static int[] timeConstraints;
    public static int totalPossibleSchedules = 0;

    @SuppressWarnings("unchecked")


    public static String createValidScheduleString(int numberOfClasses, int[] timeConstraintsInput,
                                                   int courseYear,
                                                   int courseSemester, String[] classNames) throws Exception {
        validSchedules = new ArrayList<>();
        classSchedules = new ArrayList[numberOfClasses];
        timeConstraints = timeConstraintsInput;
        for (int i = 0; i < numberOfClasses; i++) {
            String url2 = "https://classes.sis.maricopa.edu/?keywords=" + classNames[i].toLowerCase()
                    + "&all_classes=false&terms%5B%5D=" + (4252 + (courseYear - 2025) * 6 + courseSemester * 2)
                    + "&institutions%5B%5D=CGC08&subject_code=&credit_career=B&credits_min=gte0&credits_max=lte9&start_hour=&end_hour=&startafter=&instructors=";
            classSchedules[i] = scrape(url2);
            if (classSchedules[i].isEmpty()) {
                throw new Exception("One or more classes input are invalid or return 0 results.");
            }
        }
        parseCombinations(numberOfClasses - 1);
        return convertValidSchedules(numberOfClasses);
    }

    public static ArrayList<Course> scrape(String url) throws IOException {
        ArrayList<Course> arrayList = new ArrayList<>();
        Document document = Jsoup.connect(url).get();
        Elements classContent = document.select(".course");
        for (Element content : classContent) {
            createCourseList(content, arrayList);
        }
        return arrayList;
    }

    /**
     * Creates a list of courses based on a table on the Find A Class website.
     *
     * @param content the content of a single class ID.
     * @param list    a list of courses.
     */
    public static void createCourseList(Element content, ArrayList<Course> list) {
        Elements classRows = content.select("table tbody tr.class-specs*");
        for (Element row : classRows) {
            String checker = row.select(".class-location div").text();
            if (checker.contains("Online") || checker.contains("Pecos") || williamsAllowed(checker)) {
                int ID = Integer.parseInt(row.select(".class-number").text());
                boolean isOnline = row.select(".class-delivery div").text().contains("Online");
                String stringOfDays = row.select(".class-days").text().trim();
                int numberOfDays = 1;
                String times = row.select(".class-times div").text();
                for (int i = 0; i < stringOfDays.length(); i++) {
                    if (stringOfDays.charAt(i) == ',') {
                        numberOfDays++;
                    }
                }
                int[][] times2 = convertAllTimes1(times, stringOfDays, numberOfDays);
                list.add(new Course(content.select("h3").text().trim().substring(0, 6), ID, isOnline, times2));
            }
        }
    }

    /**
     * Converts the valid schedules into an easily readable format. Also, if certain
     * courses are online, the user is notified of that.
     *
     * @param numberOfClasses the number of classes.
     * @return an easily digestible format for the possible schedules.
     */
    public static String convertValidSchedules(int numberOfClasses) {
        StringBuilder output = new StringBuilder();
        output.append("\n============ POSSIBLE SCHEDULES ============\n");
        if (validSchedules.isEmpty()) {
            output.append("No possible combinations, sorry!\n");
            for (int i = 0; i < numberOfClasses; i++) {
                if (classSchedules[i].getLast().isOnline()) {
                    output.append(classSchedules[i].getFirst().name()).append(" is available online! Retry the schedule creator without this class.\n");
                }
            }
        } else {
            for (int i = 0; i < validSchedules.size(); i++) {
                output.append("Schedule ").append(i + 1).append(":\n").append(validSchedules.get(i)).append("\n");
            }
        }
        return output.toString();
    }

    /**
     * Checks if the Williams campus is allowed in the search.
     *
     * @param checker a {@code String} containing the declaration of this timeslot's
     *                location.
     * @return whether the Williams campus is allowed.
     */
    private static boolean williamsAllowed(String checker) {
        if (campusSelection == Constants.WILLIAMS) {
            return checker.contains("Williams");
        }
        return false;
    }

    /**
     * Calls the other parseCombinations method, but prevents the temporary
     * listOfCourses from existing outside the scope of this method.
     *
     * @param numberOfLevels the number of levels the parser will need to iterate
     *                       through.
     */
    public static void parseCombinations(int numberOfLevels) {
        ListOfCourses<Course> listOfCourses = new ListOfCourses<>();
        parseCombinations(listOfCourses, numberOfLevels);
    }

    /**
     * Parses all the in-person combinations possible and, if it finds a valid
     * schedule, then adds it to the list of valid schedules.
     *
     * @param listOfCourses a list of courses.
     * @param level         the level at which we are currently on. This number
     *                      decreases per loop, until we reach the final level.
     */
    public static void parseCombinations(ListOfCourses<Course> listOfCourses, int level) {
        for (int i = 0; i < classSchedules[level].size(); i++) {
            listOfCourses.add(classSchedules[level].get(i));
            if (!ListOfCourses.containsOnlineCourse(listOfCourses)) {
                if (level == 0 && isValidList(listOfCourses, timeConstraints)) {
                    validSchedules.add(new ListOfCourses<>(listOfCourses));
                } else if (level != 0) {
                    parseCombinations(listOfCourses, level - 1);
                }
            }
            listOfCourses.removeLast();
        }
    }

    private static boolean isValidList(ListOfCourses<Course> list, int[] constraints) {
        Course course;
        for (int i = 1; i < list.size(); i++) {
            course = list.get(i);
            for (int j = 0; j < i; j++) {
                if (!isCompatible(course, list.get(j))) {
                    return false;
                }
            }
        }
        for (Course c : list) {
            if (!isWithinTimeConstraints(c, constraints)) {
                AppModel.totalPossibleSchedules++;
                return false;
            }
        }
        AppModel.totalPossibleSchedules++;
        return true;
    }

    /**
     * Checks to see if the timeslot is within the time constraints.
     *
     * @param course      a course's timeslot.
     * @param constraints the time constraints set by the user.
     * @return a {@code boolean} stating whether this course's timeslots were within
     * the time constraints.
     */
    private static boolean isWithinTimeConstraints(Course course, int[] constraints) {
        int courseStartTime = course.times()[0][0] % 10000;
        int courseEndTime = course.times()[0][1] % 10000;
        return (courseStartTime >= constraints[0] && courseEndTime <= constraints[1]);
    }

    private static boolean isCompatible(Course course1, Course course2) {
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

    /**
     * Converts the time to military standard to make them easier to compare with
     * other classes and determine class compatibility.
     *
     * @param timeString the {@code String} representation of the times the class
     *                   will meet.
     * @return an {@code int[]} array of the start and end times.
     */
    public static int[] convertToMilitaryTime(String timeString) {
        int[] listOfTimes = new int[]{0, 0};
        if (timeString.charAt(0) == 'N') {
            return listOfTimes;
        }
        Scanner scan = new Scanner(timeString);
        scan.useDelimiter(" ");
        String line;
        Scanner scan2;
        for (int i = 0; i < 2; i++) {
            line = scan.next();
            scan2 = new Scanner(line);
            scan2.useDelimiter("[:AMP]+");
            listOfTimes[i] = (line.endsWith("PM") && !line.startsWith("12"))
                    ? (Integer.parseInt(scan2.next()) + 12) * 100 + Integer.parseInt(scan2.next())
                    : Integer.parseInt(scan2.next()) * 100 + Integer.parseInt(scan2.next());
            if (scan.hasNext()) {
                scan.next();
            }
            scan2.close();
        }
        scan.close();
        return listOfTimes;
    }

    /**
     * Converts each time in the matrix to a time that matches its day to make the
     * later comparisons simpler.
     *
     * @param list the complete list of times.
     * @param days a string representation of the days, such as M,W.
     * @return a converted int matrix with all the times converted.
     */
    public static int[][] convertAllTimes(String[] list, String days) {
        Scanner scan = new Scanner(days);
        scan.useDelimiter(",");
        int[][] output = new int[list.length][1];
        for (int i = 0; i < output.length; i++) {
            output[i] = convertToDay(convertToMilitaryTime(list[i]), scan.next());
        }
        scan.close();
        return output;
    }
    public static int[][] convertAllTimes1(String times, String days, int numberOfDays) {
        String[] list = new String[numberOfDays];
        Scanner scan = new Scanner(days);
        scan.useDelimiter(",");
        Arrays.fill(list, times);
        int[][] output = new int[list.length][1];
        for (int i = 0; i < output.length; i++) {
            output[i] = convertToDay(convertToMilitaryTime(list[i]), scan.next());
        }
        scan.close();
        return output;
    }

    /**
     * Converts each military time to a time that includes a representation of the
     * day. For example, if the class happens on a Monday, the time 800
     * (representing 8:00 AM) will now display 10800. If it was Tuesday, then 20800.
     * Wednesday, 30800, and so on. This makes it easier for the program to perform
     * compatibility checks later.
     *
     * @param times a list of the start and end times.
     * @param day   a {@code String} representation of the day that this timeslot
     *              takes place
     *              on.
     * @return a converted list of start and end times.
     */
    public static int[] convertToDay(int[] times, String day) {
        int[] output = new int[times.length];
        int change = switch (day) {
            case "M" -> 10000;
            case "Tu" -> 20000;
            case "W" -> 30000;
            case "Th" -> 40000;
            case "F" -> 50000;
            default -> 0;
        };
        for (int i = 0; i < output.length; i++) {
            output[i] = times[i] + change;
        }
        return output;
    }

    public static void setSemester(String semester) {
        switch (semester) {
            case "fall":
                semesterSelection = Constants.FALL;
                break;
            case "spring":
                semesterSelection = Constants.SPRING;
                break;
            case "summer":
                semesterSelection = Constants.SUMMER;
                break;
        }
    }

    public static int getSemester() {
        return switch (semesterSelection) {
            case Constants.SUMMER -> 1;
            case Constants.FALL -> 2;
            default -> 0;
        };
    }

    public static void setCampus(String campus) {
        switch (campus) {
            case "pecos":
                campusSelection = Constants.PECOS;
                break;
            case "pecosAndWilliams":
                campusSelection = Constants.WILLIAMS;
        }
    }

    public static ArrayList<ListOfCourses<Course>> getValidSchedules() {
        return validSchedules;
    }

    public boolean isValidName(String className) {
        if (className.length() != 6) {
            return false;
        }
        char[] letters = className.toCharArray();
        for (int i = 0; i < 3; i++) {
            if (!Character.isLetter(letters[i])) {
                return false;
            }
        }
        for (int i = 3; i < 6; i++) {
            if (!Character.isDigit(letters[i])) {
                return false;
            }
        }
        return true;
    }

    public void fillComboBox(DefaultComboBoxModel<String> boxModel) throws IOException {
        Document document = Jsoup.connect("https://classes.sis.maricopa.edu/").userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36").get();
        Elements subjectList = document.select("select#subject_code option");
        for (Element subjectName : subjectList) {
            boxModel.addElement(subjectName.text().substring(0, 3));
        }
        boxModel.removeElementAt(0);
    }
}