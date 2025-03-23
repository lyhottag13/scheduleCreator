package com.scraper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Hello world!
 */
public class App {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        ArrayList<ListOfClasses<Course>> goodList = new ArrayList<ListOfClasses<Course>>();
        Scanner scan = new Scanner(System.in);
        String[] listOfNames;
        // String url1 = "https://books.toscrape.com/";
        
        System.out.println("=================================================");
        System.out.println("How many classes do you need to schedule?");
        ArrayList<Course>[] array = new ArrayList[scan.nextInt()];
        listOfNames = new String[array.length];
        scan.nextLine();
        System.out.println("Enter your classes, line-by-line! (Format: AAA###)");
        for (int i = 0; i < listOfNames.length; i++) {
            listOfNames[i] = scan.nextLine();
        }
        for (int i = 0; i < listOfNames.length; i++) {
            String url2 = "https://classes.sis.maricopa.edu/?keywords=" + listOfNames[i].toLowerCase() + "&all_classes=false&terms%5B%5D=4252&terms%5B%5D=4254&terms%5B%5D=4256&institutions%5B%5D=CGC08&subject_code=&credit_career=B&credits_min=gte0&credits_max=lte9&start_hour=&end_hour=&startafter=&instructors=";
            array[i] = scrape(url2);
            System.out.println(array[i].toString());
        }
        ListOfClasses<Course> listOfClasses = new ListOfClasses<Course>();
        for (int i = 0; i < array[0].size(); i++) {
            recurse(listOfClasses, listOfNames.length - 1, array, goodList);
        }
        System.out.println(goodList);
        // System.out.println("Enter a class ID to search for! (Format: AAA###)");
        // scrape(url2, 1);
        // for (ListOfClasses<Course> l : goodList) {
        //     System.out.println(l);
        // }
        scan.close();
    }
    /**
     * 
     * @param url
     * @return
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
                if (checker.contains("Online") || checker.contains("Pecos")) {
                    int ID = Integer.parseInt(row.select(".class-number").text());
                    boolean isOnline = row.select(".class-delivery div").text().contains("Online");
                    int[] times = convertTime(row.select(".class-times div").text());
                    arrayList.add(new Course(ID, isOnline, times));
                }
            }
        }
        return arrayList;
    }
    /**
     * Converts the time to military standard to make them easier to compare with other classes and determine class compatibility.
     * @param timeString The 
     * @return An int array 
     */
    public static int[] convertTime(String timeString) {
        if (timeString.charAt(0) == 'N')
            return new int[]{0, 0};
        int[] listOfTimes = new int[2];
        Scanner scan = new Scanner(timeString);
        scan.useDelimiter(" ");
        // System.out.print(scan.next() + scan.next());
        String line = scan.next();
        Scanner scan2 = new Scanner(line);
        scan2.useDelimiter("[:AMPM]+");
        listOfTimes[0] = (line.endsWith("PM") && !line.startsWith("12")) ? (Integer.parseInt(scan2.next()) + 12) * 100 + Integer.parseInt(scan2.next()) : Integer.parseInt(scan2.next()) * 100 + Integer.parseInt(scan2.next());
        scan2.close();
        scan.next();
        line = scan.next();
        scan2 = new Scanner(line);
        scan2.useDelimiter("[:AMPM]+");
        listOfTimes[1] = (line.endsWith("PM") && !line.startsWith("12")) ? (Integer.parseInt(scan2.next()) + 12) * 100 + Integer.parseInt(scan2.next()) : Integer.parseInt(scan2.next()) * 100 + Integer.parseInt(scan2.next());
        scan.close();
        scan2.close();
        return listOfTimes;
    }
    public static void recurse(ListOfClasses<Course> list, int level, ArrayList<Course>[] array, ArrayList<ListOfClasses<Course>> goodList) {
        // if (level == -1) {
        //     goodList.add(list);
        //     list.remove();
        // } else {
        //     System.out.println(level);
        //     for (int i = 0; i < array[level].size(); i++) {
        //         if (list.add(array[level].get(i)) == false) {
        //             list.remove();
        //             continue;
        //         }
        //         recurse(list, level - 1, array, goodList);
        //     }
        // }
        if (level == 0) {
            goodList.add(new ListOfClasses<Course>(list));
            list.removeLast();
        } else {
            for (int i = 0; i < array[level - 1].size(); i++) {
                if (ListOfClasses.isValid(list, array[level - 1].get(i))) {
                    // System.out.println(array[level - 1].get(i));
                    list.add(new Course(array[level - 1].get(i)));
                    recurse(list, level - 1, array, goodList);
                } else {
                    list.removeLast();
                }
            }
        }
    }
}
