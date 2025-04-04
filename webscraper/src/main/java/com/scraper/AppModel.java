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
        PECOS, WILLIAMS,
    }

    private static Constants campusSelection;
    private static ListOfCourses<Course>[] classSchedules;
    private static ArrayList<ListOfCourses<Course>> validSchedules;
    private static int[] timeConstraints;
    private static int semesterValue;
    public static int totalPossibleSchedules = 0;

    @SuppressWarnings("unchecked")


    public void createValidSchedules(int numberOfClasses, int[] timeConstraintsInput, int courseSemester, String[] classNames) throws Exception {
        boolean invalidScrape = false;
        StringBuilder invalidClasses = new StringBuilder();
        validSchedules = new ArrayList<>();
        classSchedules = new ListOfCourses[numberOfClasses];
        timeConstraints = timeConstraintsInput;
        for (int i = 0; i < numberOfClasses; i++) {
            String url2 = "https://classes.sis.maricopa.edu/?keywords=" + classNames[i].toLowerCase() + "&all_classes=false&terms%5B%5D=" + courseSemester + "&institutions%5B%5D=CGC08&subject_code=&credit_career=B&credits_min=gte0&credits_max=lte9&start_hour=&end_hour=&startafter=&instructors=";
            classSchedules[i] = scrapeClassSchedules(url2);
            if (classSchedules[i].isEmpty()) {
                invalidScrape = true;
                invalidClasses.append("    ").append(classNames[i]).append("\n");
            }
        }
        if (invalidScrape) {
            throw new Exception("The following classes were invalid." + "\n" + invalidClasses);
        }
        parseCombinations(numberOfClasses - 1);
        findOnlineClasses(numberOfClasses);
    }

    private ListOfCourses<Course> scrapeClassSchedules(String url) throws IOException {
        ListOfCourses<Course> list = new ListOfCourses<>();
        Document document = scanWebsite(url);
        Elements classContent = document.select(".course");
        for (Element content : classContent) {
            createCourseList(content, list);
        }
        return list;
    }

    /**
     * Creates a list of courses based on a table on the Find A Class website.
     *
     * @param content the content of a single class ID.
     * @param list    a list of courses.
     */
    private void createCourseList(Element content, ListOfCourses<Course> list) {
        String name = content.select("h3").text().trim().substring(0, 6);
        Elements classRows = content.select("table tbody tr.class-specs*");
        for (Element row : classRows) {
            String checker = row.select(".class-location div").text();
            if (checker.contains("Online") || checker.contains("Pecos") || williamsAllowed(checker)) {
                String ID = row.select(".class-number").text();
                boolean isOnline = row.select(".class-delivery div").text().contains("Online");
                String times = row.select(".class-times div").text();
                String stringOfDays = row.select(".class-days").text().trim();
                String instructor = row.select(".class-instructors li").text();
                String location = row.select(".class-location div").text().substring(18);
                list.add(new Course(name, ID, isOnline, times, stringOfDays, instructor, location));
            }
        }
    }

    /**
     * Tells the user if certain courses are online.
     *
     * @param numberOfClasses the number of classes.
     * @return a {@code String} stating which classes were online.
     */
    public String findOnlineClasses(int numberOfClasses) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < numberOfClasses; i++) {
            if (classSchedules[i].getLast().isOnline()) {
                output.append(classSchedules[i].getFirst().name()).append(" is also available online!  ");
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
    private boolean williamsAllowed(String checker) {
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
    private void parseCombinations(int numberOfLevels) {
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
    private void parseCombinations(ListOfCourses<Course> listOfCourses, int level) {
        for (int i = 0; i < classSchedules[level].size(); i++) {
            listOfCourses.add(classSchedules[level].get(i));
            if (!listOfCourses.containsOnlineCourse()) {
                if (level == 0 && isValidList(listOfCourses, timeConstraints)) {
                    validSchedules.add(new ListOfCourses<>(listOfCourses));
                } else if (level != 0) {
                    parseCombinations(listOfCourses, level - 1);
                }
            }
            listOfCourses.removeLast();
        }
    }

    private boolean isValidList(ListOfCourses<Course> list, int[] constraints) {
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
    private boolean isWithinTimeConstraints(Course course, int[] constraints) {
        int courseStartTime = convertAllTimes(course.times(), course.days())[0][0] % 10000;
        int courseEndTime = convertAllTimes(course.times(), course.days())[0][1] % 10000;
        return courseStartTime >= constraints[0] && courseEndTime <= constraints[1];
    }

    private boolean isCompatible(Course course1, Course course2) {
        int[][] times1 = convertAllTimes(course1.times(), course1.days());
        int[][] times2 = convertAllTimes(course2.times(), course2.days());
        for (int[] row1 : times1) {
            for (int i1 : row1) {
                for (int[] row2 : times2) {
                    if (i1 > row2[0] && i1 < row2[1]) return false;
                }
            }
        }
        for (int[] row2 : times2) {
            for (int i2 : row2) {
                for (int[] row1 : times1) {
                    if (i2 > row1[0] && i2 < row1[1]) return false;
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
    public int[] convertToMilitaryTime(String timeString) {
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
            listOfTimes[i] = (line.endsWith("PM") && !line.startsWith("12")) ? (Integer.parseInt(scan2.next()) + 12) * 100 + Integer.parseInt(scan2.next()) : Integer.parseInt(scan2.next()) * 100 + Integer.parseInt(scan2.next());
            if (scan.hasNext()) {
                scan.next();
            }
            scan2.close();
        }
        scan.close();
        return listOfTimes;
    }

    /**
     * Converts the time for the timeslot into a converted military time for each day. This is done to make the compatibility checks between courses easier.
     *
     * @param times the start and end times for the timeslot, as "XX:XXAM - XX:XXPM".
     * @param days  the days which the class will be meeting, as "M,W,F"
     * @return an {@code int[]} representing the timeslot's beginning and end times in military time, with modifications made based on its day.
     */
    private int[][] convertAllTimes(String times, String days) {
        int numberOfDays = 1;
        for (int i = 0; i < days.length(); i++) {
            if (days.charAt(i) == ',') {
                numberOfDays++;
            }
        }
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
    private int[] convertToDay(int[] times, String day) {
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

    public void setSemesterValue(int value) {
        semesterValue = value;
    }

    public int getSemesterValue() {
        return semesterValue;
    }

    public void setCampus(String campus) {
        switch (campus) {
            case "pecos":
                campusSelection = Constants.PECOS;
                break;
            case "pecosAndWilliams":
                campusSelection = Constants.WILLIAMS;
        }
    }

    public ArrayList<ListOfCourses<Course>> getValidSchedules() {
        return validSchedules;
    }

    public ListOfCourses<Course>[] getClassSchedules() {
        return classSchedules;
    }

    /**
     * Checks to see if the name of a class inputted is valid. For example, a class name of "AAA000" would be valid since it has all the properties of an actual class name.
     *
     * @param className the name of the class to be checked.
     * @return whether the class name is valid.
     */
    public boolean isValidName(String className) {
        if (className.length() != 6) {
            return false;
        }
        char[] letters = className.toCharArray();
        for (int i = 3; i < 6; i++) {
            if (!Character.isDigit(letters[i])) {
                return false;
            }
        }
        return true;
    }

    public void fillComboBox(Document document, AppView view) {
        Elements subjectList = document.select("select#subject_code option");
        for (Element subjectName : subjectList) {
            view.getComboBoxModel().addElement(subjectName.text());
        }
        view.getComboBoxModel().removeElementAt(0);
    }

    public void setSemesterButtons(Document document, AppView view) {
        Elements semesterList = document.select("#terms .form-check");
        int i = 0;
        for (Element semester : semesterList) {
            JRadioButton button = view.getSemesterRadioButtons()[i];
            button.setName(semester.select("input").attr("value"));
            button.setText(semester.select("label").text());
            button.addActionListener(e -> setSemesterValue(Integer.parseInt(button.getName())));
            i++;
        }
        view.getSemesterRadioButtons()[0].doClick();
    }

    private Document scanWebsite(String url) throws IOException {
        return Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36").get();
    }

    public void createAddPanelAndSemesterButtons(AppView view) throws IOException {
        Document document = scanWebsite("https://classes.sis.maricopa.edu");
        fillComboBox(document, view);
        setSemesterButtons(document, view);
    }
}