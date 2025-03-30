package com.scraper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Scraper {
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
    /**
     * Returns the valid schedules for the user's inputs. This is different from the
     * App.java
     * class, instead working specifically based on the GUI's specifications.
     * 
     * @param numberOfClasses      the number of classes in the user's input.
     * @param timeConstraintsInput the time constraints as an array.
     * @param campus               the desired campus; either Pecos or Pecos &
     *                             Williams
     * @param courseYear           the desired year.
     * @param courseSemester       the desired semester.
     * @param classNames           the names of all the desired courses.
     * @return
     */
    public static String scrapeValidSchedules(int numberOfClasses, int[] timeConstraintsInput, int campus,
            int courseYear,
            int courseSemester, String[] classNames) {
        validSchedules = new ArrayList<>();
        classSchedules = new ArrayList[numberOfClasses];
        timeConstraints = timeConstraintsInput;
        for (int i = 0; i < numberOfClasses; i++) {
            String url2 = "https://classes.sis.maricopa.edu/?keywords=" + classNames[i].toLowerCase()
                    + "&all_classes=false&terms%5B%5D=" + (4250 + (courseYear - 2024) * 6 + courseSemester)
                    + "&institutions%5B%5D=CGC08&subject_code=&credit_career=B&credits_min=gte0&credits_max=lte9&start_hour=&end_hour=&startafter=&instructors=";
            try {
                classSchedules[i] = scrape(url2);
                System.out.println(classSchedules[i]);
            } catch (Exception e) {
                System.out.println("INVALID SCRAPE");
                e.printStackTrace();
            }
        }
        parseCombinations(numberOfClasses - 1);
        return convertValidSchedules(numberOfClasses);
    }

    public static ArrayList<Course> scrape(String url) throws IOException {
        ArrayList<Course> arrayList = new ArrayList<Course>();
        Document document = Jsoup.connect(url).get();
        Elements classContent = document.select(".course");
        for (Element content : classContent) {
            createCourseList(content, arrayList);
        }
        return arrayList;
    }

    public static void createCourseList(Element content, ArrayList<Course> list) {
        Elements classRows = content.select("table tbody tr.class-specs*");
        for (Element row : classRows) {
            String checker = row.select(".class-location div").text();
            if (checker.contains("Online") || checker.contains("Pecos") || williamsAllowed(checker)) {
                int ID = Integer.parseInt(row.select(".class-number").text());
                boolean isOnline = row.select(".class-delivery div").text().contains("Online");
                String stringOfDays = row.select(".class-days").text().trim();
                int numberOfDays = 1;
                for (int i = 0; i < stringOfDays.length(); i++) {
                    if (stringOfDays.charAt(i) == ',') {
                        numberOfDays++;
                    }
                }
                String[] times1 = new String[numberOfDays];
                for (int i = 0; i < numberOfDays; i++) {
                    times1[i] = row.select(".class-times div").text();
                }
                int[][] times2 = convertAllTimes(times1, stringOfDays);
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
        if (validSchedules.size() == 0) {
            output.append("No possible combinations, sorry! Retry with different limits.\n");
            for (int i = 0; i < numberOfClasses; i++) {
                if (classSchedules[i].get(classSchedules[i].size() - 1).isOnline()) {
                    output.append(classSchedules[i].get(0).name() + " is avaliable online!\n");
                }
            }
        } else {
            for (int i = 0; i < validSchedules.size(); i++) {
                output.append("Schedule " + (i + 1) + ":\n" + validSchedules.get(i) + "\n");
            }
        }
        return output.toString();
    }

    /**
     * Checks if the Williams campus is allowed in the search.
     * 
     * @param checker a {@code String} containing the declaration of this timeslot's
     *                location.
     * @return whether or not the Williams campus is allowed.
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
        ListOfCourses<Course> listOfCourses = new ListOfCourses<Course>();
        parseCombinations(listOfCourses, numberOfLevels);
    }

    /**
     * Parses all the in-person combinations possible and, if it finds a valid
     * schedule, then
     * adds it to the list of valid schedules.
     * 
     * @param listOfCourses a list of courses.
     * @param level         the level at which we are currently on. This number
     *                      decreases per loop, until we reach the final level.
     */
    public static void parseCombinations(ListOfCourses<Course> listOfCourses, int level) {
        for (int i = 0; i < classSchedules[level].size(); i++) {
            listOfCourses.add(classSchedules[level].get(i));
            if (!ListOfCourses.containsOnlineCourse(listOfCourses)) {
                if (level == 0 && ListOfCourses.isValidList(listOfCourses, timeConstraints)) {
                    validSchedules.add(new ListOfCourses<Course>(listOfCourses));
                } else if (level != 0) {
                    parseCombinations(listOfCourses, level - 1);
                }
            }
            listOfCourses.removeLast();
        }
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
        int[] listOfTimes = new int[] { 0, 0 };
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
            scan2.useDelimiter("[:AMPM]+");
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
        int change;
        switch (day) {
            case "M":
                change = 10000;
                break;
            case "Tu":
                change = 20000;
                break;
            case "W":
                change = 30000;
                break;
            case "Th":
                change = 40000;
                break;
            case "F":
                change = 50000;
                break;
            default:
                change = 0;
        }
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
        switch (semesterSelection) {
            case Constants.FALL:
                return 0;
            case Constants.SPRING:
                return 1;
            case Constants.SUMMER:
                return 2;
            default:
                return 0;
        }
    }

    public static void setCampus(String campus) {
        switch (campus) {
            case "pecos":
                campusSelection = Constants.PECOS;
                break;
            case "williams":
                campusSelection = Constants.WILLIAMS;
        }
    }

    public static int getCampus() {
        return 1;
    }

    public static ArrayList<ListOfCourses<Course>> getValidSchedules() {
        return validSchedules;
    }
}