package com.scraper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class App {
    private static int totalPossibleSchedules = 0;
    private static ArrayList<Course>[] classSchedules;
    private static Scanner scan;
    private static String[] classNames;
    private static int[] timeConstraints;
    private static boolean williamsIncluded;
    private static ArrayList<ListOfCourses<Course>> validSchedules;
    private static int numberOfClasses;
    private static int courseYear;
    private static int courseSemester;

    public static void main(String[] args) {
        scan = new Scanner(System.in);
        courseYear = -1;
        courseSemester = -1;
        while (true) {
            validSchedules = new ArrayList<ListOfCourses<Course>>();
            totalPossibleSchedules = 0;
            System.out.println("=================================================");
            readUserInputs();

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

            try {
                parseCombinations(numberOfClasses - 1);
            } catch (Exception e) {
                System.out.println("INVALID PARSE");
                e.printStackTrace();
            }

            printValidSchedules();
            System.out.println("Based on your time filters, I displayed " + validSchedules.size() + " of "
                    + totalPossibleSchedules + " possible schedules.");
            System.out.println("Would you like to try again? Y/N");
            String yesOrNo = scan.nextLine().toLowerCase();
            if (yesOrNo.equals("n")) {
                break;
            }
        }
        scan.close();
    }

    /**
     * Scans all the user inputs for their desired classes. If I come up with more
     * valid stipulations, I can just add it to the list here.
     */
    public static void readUserInputs() {
        readNumberOfClasses();
        readTimeLimits();
        readCampus();
        readYear();
        readSemester();
        readClassNames();
    }

    @SuppressWarnings("unchecked")
    public static void readNumberOfClasses() {
        System.out.println("How many in-person classes do you need to schedule?");
        while (true) {
            try {
                numberOfClasses = Integer.parseInt(scan.nextLine());
                if (numberOfClasses <= 0 || numberOfClasses > 7) {
                    throw new Exception();
                }
                break;
            } catch (Exception e) {
                System.out.println("Enter a valid number!");
            }
        }
        classSchedules = new ArrayList[numberOfClasses];
        classNames = new String[numberOfClasses];
    }

    public static void readTimeLimits() {
        System.out.println(
                "What's your earliest and latest times? (Format: ####, as in military time. Enter in two different lines)");
        timeConstraints = new int[2];
        while (true) {
            try {
                for (int i = 0; i < timeConstraints.length; i++) {
                    timeConstraints[i] = Integer.parseInt(scan.nextLine());
                }
                if (timeConstraints[0] > timeConstraints[1] || timeConstraints[0] < 0 || timeConstraints[1] > 2400) {
                    throw new Exception();
                }
                break;
            } catch (Exception e) {
                System.out.println("Enter valid times!");
            }
        }
    }

    public static void readCampus() {
        System.out.println(
                "Would you like your classes to be all-Pecos or a mix of Pecos and Williams? (P for Pecos, W for Williams and Pecos");
        while (true) {
            char temp;
            try {
                temp = scan.nextLine().toLowerCase().charAt(0);
                if (temp == 'p') {
                    williamsIncluded = false;
                    break;
                } else if (temp == 'w') {
                    williamsIncluded = true;
                    break;
                }
            } catch (Exception e) {
                System.out.println("Enter a valid input!");
            }
        }
    }

    public static void readYear() {
        if (courseYear != -1) {
            return;
        }
        System.out.println("What year are your courses?");
        while (true) {
            try {
                courseYear = Integer.parseInt(scan.nextLine());
                if (courseYear < 2025 || courseYear > 2100) {
                    throw new Exception();
                }
                break;
            } catch (Exception e) {
                System.out.println("Enter a valid input!");
            }
        }
    }

    public static void readSemester() {
        if (courseSemester != -1) {
            return;
        }
        System.out.println("What semester are your courses? (Fall, Spring, Summer)");
        while (true) {
            String semesterCalculator;
            try {
                semesterCalculator = scan.nextLine().toLowerCase();
                switch (semesterCalculator) {
                    case "autumn":
                    case "fall":
                        courseSemester = 0;
                        break;
                    case "spring":
                        courseSemester = 2;
                        break;
                    case "summer":
                        courseSemester = 4;
                        break;
                    default:
                        throw new Exception();
                }
                break;
            } catch (Exception e) {
                System.out.println("Enter a valid input!");
            }
        }
    }

    public static void readClassNames() {
        System.out.println("Enter your classes, line-by-line! (Format: AAA###)");
        while (true) {
            try {
                for (int i = 0; i < classNames.length; i++) {
                    classNames[i] = scan.nextLine();
                    if (classNames[i].length() > 6) {
                        throw new Exception();
                    }
                }
                break;
            } catch (Exception e) {
                System.out.println("Enter valid inputs!");
            }
        }
    }

    /**
     * Scrapes the Maricopa Find A Class website to get all the time slots for a
     * course.
     * 
     * @param url the url of the site that will be scraped.
     * @return an ArrayList representation of all the possible time slots for a
     *         single course.
     * @throws IOException
     */
    public static ArrayList<Course> scrape(String url) throws IOException {
        ArrayList<Course> arrayList = new ArrayList<Course>();
        Document document = Jsoup.connect(url).get();
        Elements classContent = document.select(".course");
        for (Element content : classContent) {
            createCourseList(content, arrayList);
        }
        return arrayList;
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
     * Creates a list of courses and adds it to the main list of courses for a
     * specific course type.
     * 
     * @param content the main content of a class, like MAT240 and the table
     *                underneath.
     * @param list    the main list that this method will add the current content's
     *                courses to.
     */
    public static void createCourseList(Element content, ArrayList<Course> list) {
        Elements classRows = content.select("table tbody tr.class-specs*");
        for (Element row : classRows) {
            String checker = row.select(".class-location div").text();
            if (checker.contains("Online") || checker.contains("Pecos")
                    || (checker.contains("Williams") && williamsIncluded)) {
                int ID = Integer.parseInt(row.select(".class-number").text());
                boolean isOnline = row.select(".class-delivery div").text().contains("Online");
                String stringOfDays = row.select(".class-days").text().trim();
                int numberOfDays = 1;
                for (int i = 0; i < stringOfDays.length(); i++) {
                    if (stringOfDays.charAt(i) == ',')
                        numberOfDays++;
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

    public static void printValidSchedules() {
        System.out.println("\n============ POSSIBLE SCHEDULES ============");
        if (validSchedules.size() == 0) {
            System.out.println("No possible combinations, sorry! Retry with different limits.");
            for (int i = 0; i < numberOfClasses; i++) {
                if (classSchedules[i].get(classSchedules[i].size() - 1).isOnline()) {
                    System.out.println(classSchedules[i].get(0).name() + " is avaliable online!");
                }
            }
        } else {
            for (int i = 0; i < validSchedules.size(); i++) {
                System.out.println("Schedule " + (i + 1) + ":\n" + validSchedules.get(i));
            }
        }
    }
    /**
     * Converts the time to military standard to make them easier to compare with
     * other classes and determine class compatibility.
     * 
     * @param timeString the string representation of the times the class will meet.
     * @return an int array of the start and end times.
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
     * Wednesday, 30800, and so on. This makes it easier for the program to make
     * compatibility checks later.
     * 
     * @param times a list of the start and end times.
     * @param day   a string representation of the day that this time takes place
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

}
