package com.scraper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class AppController {
    AppView view;
    AppModel model;

    public AppController(AppView view, AppModel model) {
        this.view = view;
        this.model = model;
        initialize();
    }

    public void initialize() {
        try {
            model.createAddPanelAndSemesterButtons(view);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not connect.", "Error", JOptionPane.ERROR_MESSAGE, null);
        }
        model.setCampus("pecos");
        view.setAddButtonListener(e -> handleAddButton());
        view.setEditButtonListener(e -> handleEditButton());
        view.setRemoveButtonListener(e -> handleRemoveButton());
        view.setTryAgainButtonListener(e -> toggleScreen());
        view.setTimeSliderListener(e -> handleTimeSlider());
        view.setTimeSlider2Listener(e -> handleTimeSlider2());
        view.setCreateButtonListener(e -> {
            if (setResultsText()) {
                toggleScreen();
            }
        });
        view.setPecosButtonListener(e -> model.setCampus("pecos"));
        view.setPecosAndWilliamsButtonListener(e -> model.setCampus("pecosAndWilliams"));
        view.setIdentificationInputKeyAdapter(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (view.getIdentificationInput().getText().length() >= 3) {
                    e.consume();
                }
            }
        });
    }

    private void handleAddButton() {
        if (JOptionPane.showConfirmDialog(null, view.getChoicePanel(), "Add a class!", JOptionPane.OK_CANCEL_OPTION) == 0) {
            String className = view.getComboBoxModel().getElementAt(view.getComboBox().getSelectedIndex()).substring(0, 3) + view.getIdentificationInput().getText();
            if (model.isValidName(className)) {
                view.getListModel().add(0, className.toUpperCase());
            } else {
                JOptionPane.showMessageDialog(null, "Enter a valid name", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        view.getIdentificationInput().setText("");
        view.getComboBox().setSelectedIndex(0);
    }

    private void handleEditButton() {
        if (view.getClassList().isSelectionEmpty()) {
            JOptionPane.showMessageDialog(null, "Select an item", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showConfirmDialog(null, view.getChoicePanel(), "What would you like to change this class to?", JOptionPane.OK_CANCEL_OPTION);
        String classLetters = view.getComboBoxModel().getElementAt(view.getComboBox().getSelectedIndex()).substring(0, 3);
        String classNumbers = view.getIdentificationInput().getText();
        String userInput = classLetters + classNumbers;
        if (!model.isValidName(userInput)) {
            System.out.println(userInput);
            JOptionPane.showMessageDialog(null, "Enter a valid name", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            view.getListModel().set(view.getClassList().getSelectedIndex(), userInput);
        }
        view.getComboBox().setSelectedIndex(0);
        view.getIdentificationInput().setText("");
    }

    private void handleRemoveButton() {
        if (view.getListModel().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Enter more items.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            if (view.getClassList().isSelectionEmpty()) {
                view.getListModel().remove(0);
            } else {
                view.getListModel().remove(view.getClassList().getSelectedIndex());
            }
        }
    }

    private void handleTimeSlider2() {
        if (view.getTimeSlider2().getValue() < view.getTimeSlider().getValue()) {
            view.getTimeSlider().setValue(view.getTimeSlider2().getValue() - 1);
        }
    }

    private void handleTimeSlider() {
        if (view.getTimeSlider().getValue() > view.getTimeSlider2().getValue()) {
            view.getTimeSlider2().setValue(view.getTimeSlider().getValue() + 1);
        }
    }

    /**
     * Sets the text of the results page to the schedules it scraped if it succeeds.
     *
     * @return a {@code boolean} that states whether the operation was successful.
     */
    private boolean setResultsText() {
        AppModel.totalPossibleSchedules = 0;
        try {
            if (view.getNumberOfCourses() == 0) {
                throw new Exception("The number of courses is invalid.");
            }
            model.createValidSchedules(view.getNumberOfCourses(), view.getTimeConstraints(), model.getSemesterValue(), view.getCourseNames());
            createTablesPanel();
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Creates the panel for all the tables, which will get added to the results screen.
     */
    private void createTablesPanel() {
        ArrayList<ListOfCourses<Course>> validSchedules = model.getValidSchedules();
        view.setTablesSize(validSchedules.size());
        JTable[] tables = view.getTables();
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        JPanel tablesPanel = new JPanel(new GridLayout(validSchedules.size() + 1, 1));
        tablesPanel.add(headerPanel);
        headerPanel.add(new JLabel("Based on your filters, I displayed " + validSchedules.size() + " of " + AppModel.totalPossibleSchedules + " schedules.") {{
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
        }});
        for (ListOfCourses<Course> list : model.getClassSchedules()) {
            if (list.containsOnlineCourse()) {
                headerPanel.add(new JLabel(model.findOnlineClasses(view.getNumberOfCourses())) {{
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setVerticalAlignment(SwingConstants.CENTER);
                }});
                break;
            }
        }
        for (int i = 0; i < validSchedules.size(); i++) {
            {
                ListOfCourses<Course> schedule = model.getValidSchedules().get(i);
                String[][] tableData = new String[schedule.size()][7];
                for (int j = 0; j < schedule.size(); j++) {
                    Course newCourse = schedule.get(j);
                    tableData[j][0] = newCourse.ID();
                    tableData[j][1] = newCourse.name();
                    tableData[j][2] = newCourse.location();
                    tableData[j][3] = newCourse.days();
                    tableData[j][4] = newCourse.times();
                    tableData[j][5] = newCourse.instructor();
                }
                tables[i] = new JTable(tableData, new String[]{"ID", "Name", "Location", "Days", "Times", "Instructor"});
                tables[i].setCellSelectionEnabled(false);
                tables[i].setFillsViewportHeight(true);
                tables[i].setDefaultEditor(Object.class, null);
                tables[i].setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                tables[i].getTableHeader().setReorderingAllowed(false);
                tables[i].getTableHeader().setResizingAllowed(false);
                tables[i].getColumnModel().getColumn(0).setPreferredWidth(5);
                tables[i].getColumnModel().getColumn(1).setPreferredWidth(6);
                tables[i].getColumnModel().getColumn(3).setPreferredWidth(1);
                tables[i].getColumnModel().getColumn(4).setPreferredWidth(100);
            }
            JScrollPane tablePane = new JScrollPane();
            int height = (int) (tables[i].getRowCount() * tables[i].getRowHeight() + tables[i].getTableHeader().getPreferredSize().getHeight() + 3);
            tablePane.setPreferredSize(new Dimension(600, height));
            tablePane.setViewportView(tables[i]);
            JPanel smallerTablePanel = new JPanel(new GridLayout(2, 1));
//            smallerTablePanel.setPreferredSize(new Dimension(AppView.FRAME_WIDTH - AppView.BORDER_SIZE * 3, height + 50));
            smallerTablePanel.add(new JLabel("Schedule " + (i + 1)) {{
                setHorizontalAlignment(SwingConstants.CENTER);
                setVerticalAlignment(SwingConstants.BOTTOM);
            }});
            smallerTablePanel.add(tablePane);
            tablesPanel.add(smallerTablePanel);
        }

        if (validSchedules.isEmpty()) {
            headerPanel.add(new JLabel("No Schedules Available! Try again with different filters.") {{
                setHorizontalAlignment(SwingConstants.CENTER);
                setVerticalAlignment(SwingConstants.CENTER);
            }});
        }
        view.setTablesPanel(tablesPanel);
    }

    private void toggleScreen() {
        ((CardLayout) view.getMainPanel().getLayout()).next(view.getMainPanel());
    }
}
