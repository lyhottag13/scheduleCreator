package com.scraper;

import java.util.LinkedList;

public class ListOfClasses<E> extends LinkedList<Course> {
    public ListOfClasses(ListOfClasses<E> list) {
        for (Course c : list) {
            add(new Course(c));
        }
    }
    public ListOfClasses() {}
    // private LinkedList<Class> list = new LinkedList<Class>();
    // private LinkedNode head;
    // private LinkedNode tail;
    // public ListOfClasses(ListOfClasses list) {
    //     LinkedNode current = list.head;
    //     while (current != null) {
    //         addClass(current.getClassElement());
    //     }
    // }
    /**
     * 
     * @param classElement The element to be added.
     * @return A boolean representing if the addition was valid.
     */
    // public boolean addClass(Course classElement) {
    //     LinkedNode newClass = new LinkedNode(classElement);
    //     if (head == null) {
    //         head = newClass;
    //         tail = newClass;
    //         size++;
    //         return true;
    //     } else {
    //         tail.next = newClass;
    //         newClass.previous = tail;
    //         tail = newClass;
    //         size++;
    //     }
    //     return isValid(this, classElement);
    // }
    public static boolean isValid(ListOfClasses<Course> list, Course newClass) {
        for (Course c : list) {
            if (!isCompatibleTime(c.times(), newClass.times()) || newClass.isOnline())
                return false;
        }
        return true;
    }
    public static boolean isCompatibleTime(int[] times1, int[] times2) {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                if (times1[j] > times2[0] && times1[j] < times2[1])
                    return false;
            }
            if (times2[i] > times1[0] && times2[i] < times1[1])
                return false;
        }
        return true;
    }
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            output.append(get(i));
        }
        return output.toString() + "\n";
    }
    // public void remove() {
    //     if (head != null) {
    //         tail = tail.previous;
    //         tail.next.previous = null;
    //         tail.next = null;
    //         size--;
    //     }
    // }
    // public LinkedNode getHead() {
    //     return head;
    // }
    // public LinkedNode getTail() {
    //     return tail;
    // }
    // public int getSize() {
    //     return size;
    // }
    // public String toString() {
    //     LinkedNode current = head;
    //     StringBuilder output = new StringBuilder();
    //     while (current != null) {
    //         output.append(current.classElement + "\n");
    //         current = current.next;
    //     }
    //     return output.toString();
    // }
    // private class LinkedNode {
    //     private LinkedNode next;
    //     private LinkedNode previous;
    //     private Course classElement;
    //     public LinkedNode(Course classElement) {
    //         this.classElement = classElement;
    //     }
    //     public Course getClassElement() {
    //         return classElement;
    //     }
    // }
}
