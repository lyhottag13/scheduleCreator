package com.scraper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class App {
    private static ArrayList<Course>[] arrayOfClassTypes;
    private static Scanner scan;
    private static String[] names;
    private static int[] timeConstraints;
    private static boolean williamsIncluded;
    private static ArrayList<ListOfCourses<Course>> goodList;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        scan = new Scanner(System.in);
        goodList = new ArrayList<ListOfCourses<Course>>();
        timeConstraints = new int[2];

        System.out.println("=================================================");
        System.out.println("How many classes do you need to schedule?");
        int numberOfClasses;
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
        arrayOfClassTypes = new ArrayList[numberOfClasses];
        names = new String[numberOfClasses];

        System.out.println(
                "What's your desired minimum and maximum times? (Format: ####, as in military time. Enter in two different lines)");
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
        System.out.println("Enter your classes, line-by-line! (Format: AAA###)");
        for (int i = 0; i < names.length; i++) {
            names[i] = scan.nextLine();
        }

        for (int i = 0; i < names.length; i++) {
            String url2 = "https://classes.sis.maricopa.edu/?keywords=" + names[i].toLowerCase()
                    + "&all_classes=false&terms%5B%5D=4252&terms%5B%5D=4254&terms%5B%5D=4256&institutions%5B%5D=CGC08&subject_code=&credit_career=B&credits_min=gte0&credits_max=lte9&start_hour=&end_hour=&startafter=&instructors=";
            try {
                arrayOfClassTypes[i] = scrape(url2);
                System.out.println(arrayOfClassTypes[i]);
            } catch (Exception e) {
                System.out.println("INVALID SCRAPE");
                e.printStackTrace();
            }
        }

        ListOfCourses<Course> temporaryList = new ListOfCourses<Course>();
        try {
            parseCombinations(temporaryList, numberOfClasses - 1);
        } catch (Exception e) {
            System.out.println("INVALID PARSE");
            e.printStackTrace();
        }

        {
            // for (int i = 0; i < arrayOfClassTypes[0].size(); i++) {
            // list.add(arrayOfClassTypes[0].get(i));
            // for (int j = 0; j < arrayOfClassTypes[1].size(); j++) {
            // list.add(arrayOfClassTypes[1].get(j));
            // for (int k = 0; k < arrayOfClassTypes[2].size(); k++) {
            // list.add(arrayOfClassTypes[2].get(k));
            // if (ListOfClasses.isValidList(list, timeConstraints)) {
            // goodList.add(new ListOfClasses<Course>(list));
            // }
            // list.removeLast();
            // }
            // list.removeLast();
            // }
            // list.removeLast();
            // }
        }

        System.out.println("\n============ POSSIBLE SCHEDULES ============");
        if (goodList.size() == 0) {
            System.out.println("No possible combinations, sorry!");
        } else {
            for (int i = 0; i < goodList.size(); i++) {
                System.out.println("Schedule " + (i + 1) + ":\n" + goodList.get(i));
            }
        }
        scan.close();
    }

    /**
     * Parses all the combinations possible and, if it finds a valid schedule, then
     * adds it to the list of valid schedules.
     * 
     * @param list  a list of courses.
     * @param level the level at which we are currently on. This number decreases
     *              per loop, until we reach the final level.
     */
    public static void parseCombinations(ListOfCourses<Course> list, int level) {
        for (int i = 0; i < arrayOfClassTypes[level].size(); i++) {
            list.add(arrayOfClassTypes[level].get(i));
            if (level == 0 && ListOfCourses.isValidList(list, timeConstraints)) {
                goodList.add(new ListOfCourses<Course>(list));
            } else if (level != 0) {
                parseCombinations(list, level - 1);
            }
            list.removeLast();
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
            Elements classRows = content.select("table tbody tr.class-specs*");
            for (Element row : classRows) {
                String checker = row.select(".class-location div").text();
                if (filterClasses(checker)) {
                    int ID = Integer.parseInt(row.select(".class-number").text());
                    boolean isOnline = row.select(".class-delivery div").text().contains("Online");
                    String stringOfDays = row.select(".class-days").text().trim();
                    int numberOfDays = 1;
                    for (int i = 0; i < stringOfDays.length(); i++) {
                        if (stringOfDays.charAt(i) == ',')
                            numberOfDays++;
                    }
                    int[][] times1 = new int[numberOfDays][1];
                    for (int i = 0; i < numberOfDays; i++) {
                        times1[i] = convertToMilitaryTime(row.select(".class-times div").text());
                    }
                    times1 = convertAllTimes(times1, stringOfDays);
                    arrayList.add(new Course(content.select("h3").text().trim().substring(0, 6), ID, isOnline, times1));
                }
            }
        }
        return arrayList;
    }

    /**
     * Filters the classes based on whether or not the user wanted a mix of courses
     * from
     * both campi, or just classes from the Pecos campus.
     * 
     * @param checker a string representation of the course's location.
     * @return a boolean that tells whether or not the class is valid based on the
     *         user's stipulations.
     */
    public static boolean filterClasses(String checker) {
        return checker.contains("Online") || checker.contains("Pecos")
                || (checker.contains("Williams") && williamsIncluded);
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
    public static int[][] convertAllTimes(int[][] list, String days) {
        Scanner scan = new Scanner(days);
        scan.useDelimiter(",");
        int[][] output = new int[list.length][1];
        for (int i = 0; i < output.length; i++) {
            output[i] = convertToDay(list[i], scan.next());
        }
        scan.close();
        return output;
    }

    /**
     * Converts each military time to a time that includes a representation of the
     * day.
     * For example, if the class happens on a Monday, the time 800 (representing
     * 8:00 AM) will now display 10800.
     * If it was Tuesday, then 20800. Wednesday, 30800, and so on. This makes it
     * easier for the comparisons to be made later.
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
