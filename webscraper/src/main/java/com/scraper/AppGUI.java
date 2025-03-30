package com.scraper;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.time.Year;

public class AppGUI implements ActionListener, ChangeListener {
    private JFrame frame;
    private JPanel backgroundPanel, settingsPanel, classSettingsPanel,
            backgroundPanel2;
    private JPanel classSettingsLeftPanel, classSettingsRightPanel;
    private JScrollPane scrollPane;
    private JList<String> classList;
    private DefaultListModel<String> listModel;
    private JRadioButton pecos, notPecos, fall, spring, summer;
    private ButtonGroup pecosOrNot, semester;
    private JSlider timeSlider, timeSlider2;
    private JButton addButton, removeButton, editButton, createButton, tryAgainButton;
    private JTextPane results;
    private JSpinner spinner;
    private final int NUMBER_OF_SETTINGS = 4;

    public AppGUI() {
        frame = new JFrame("Schedule Creator");
        backgroundPanel = new JPanel(new BorderLayout(0, 50));
        backgroundPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        settingsPanel = new JPanel(new GridLayout((int) Math.ceil(NUMBER_OF_SETTINGS / 2.0), 2, 50, 10));
        settingsPanel.setBorder(new TitledBorder("Settings"));
        listModel = new DefaultListModel<>();
        classList = new JList<>(listModel);
        timeSlider = new JSlider(6, 22, 6);
        timeSlider2 = new JSlider(6, 22, 22);
        // Class Settings.
        {
            classSettingsPanel = new JPanel(new GridLayout(1, 2, 10, 50));
            classSettingsPanel.setBorder(new TitledBorder("Classes"));
            classSettingsLeftPanel = new JPanel(new BorderLayout());
            classSettingsRightPanel = new JPanel(new GridLayout(3, 1));
            addButton = new JButton("Add");
            removeButton = new JButton("Remove");
            editButton = new JButton("Edit");
            addButton.addActionListener(this);
            removeButton.addActionListener(this);
            editButton.addActionListener(this);
            classSettingsRightPanel.add(addButton);
            classSettingsRightPanel.add(removeButton);
            classSettingsRightPanel.add(editButton);
            // classSettingsLeftPanel.add(new JLabel("Classes (AAA###)"));
            classSettingsLeftPanel.add(classList);
            classSettingsPanel.add(classSettingsLeftPanel);
            classSettingsPanel.add(classSettingsRightPanel);
        }

        createButton = new JButton("Create");
        createButton.addActionListener(this);
        // Regular Settings
        {
            JPanel[] panelsInSettings = new JPanel[NUMBER_OF_SETTINGS];
            for (int i = 0; i < NUMBER_OF_SETTINGS; i++) {
                panelsInSettings[i] = new JPanel(new BorderLayout());
                panelsInSettings[i].setBorder(new LineBorder(new Color(0, 0, 0)));
            }
            JLabel[] labels = new JLabel[NUMBER_OF_SETTINGS];
            labels[0] = new JLabel("Year");
            labels[1] = new JLabel("Time (Military Format, 2:00 PM = 1400)");
            labels[2] = new JLabel("Semester");
            labels[3] = new JLabel("Campus");

            spinner = new JSpinner(new SpinnerNumberModel(Year.now().getValue(), Year.now().getValue(), Year.now().getValue() + 1, 1));
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0");
            editor.getTextField().setEditable(false);
            spinner.setEditor(editor);

            // Radio Buttons.
            pecos = new JRadioButton("Pecos");
            notPecos = new JRadioButton("Pecos & Williams");
            fall = new JRadioButton("Fall");
            spring = new JRadioButton("Spring");
            summer = new JRadioButton("Summer");
            fall.addActionListener(this);
            spring.addActionListener(this);
            summer.addActionListener(this);
            pecosOrNot = new ButtonGroup();
            semester = new ButtonGroup();
            JPanel campusRadioPanel = new JPanel();
            pecosOrNot.add(pecos);
            pecosOrNot.add(notPecos);
            campusRadioPanel.add(pecos);
            campusRadioPanel.add(notPecos);
            JPanel semesterRadioPanel = new JPanel();
            semester.add(fall);
            semester.add(spring);
            semester.add(summer);
            semesterRadioPanel.add(fall);
            semesterRadioPanel.add(spring);
            semesterRadioPanel.add(summer);
            fall.setSelected(true);
            pecos.setSelected(true);
            Scraper.setCampus("pecos");
            Scraper.setSemester("fall");

            // Sliders
            timeSlider.setPaintTicks(true);
            timeSlider.setMajorTickSpacing(1);
            timeSlider.setPaintLabels(true);
            timeSlider.setSnapToTicks(true);
            timeSlider.setPaintTrack(false);

            timeSlider2.setPaintTicks(true);
            timeSlider2.setMajorTickSpacing(1);
            timeSlider2.setPaintLabels(true);
            timeSlider2.setSnapToTicks(true);
            timeSlider2.setPaintTrack(false);

            timeSlider.addChangeListener(this);
            timeSlider2.addChangeListener(this);

            JPanel sliderHolder = new JPanel(new GridLayout(2, 1));
            sliderHolder.add(timeSlider);
            sliderHolder.add(timeSlider2);

            // This adds the components and their labels together.
            JComponent[] doodads = new JComponent[NUMBER_OF_SETTINGS];
            doodads[0] = spinner;
            doodads[1] = sliderHolder;
            doodads[2] = semesterRadioPanel;
            doodads[3] = campusRadioPanel;
            for (int i = 0; i < NUMBER_OF_SETTINGS; i++) {
                panelsInSettings[i].add(labels[i], BorderLayout.NORTH);
                panelsInSettings[i].add(doodads[i], BorderLayout.CENTER);
                settingsPanel.add(panelsInSettings[i]);
            }
        }

        // Screen 2, the one that shows the results.
        {
            tryAgainButton = new JButton("Try Again?");
            tryAgainButton.addActionListener(this);
    
            results = new JTextPane();
            results.setEditable(false);
            scrollPane = new JScrollPane();
            scrollPane.setViewportView(results);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setPreferredSize(new Dimension(500, 500));
            backgroundPanel2 = new JPanel(new BorderLayout());
            backgroundPanel2.add(new JLabel("Possible Schedules"));
            backgroundPanel2.add(scrollPane);
            backgroundPanel2.add(tryAgainButton, BorderLayout.SOUTH);
    
        }

        settingsPanel.setPreferredSize(new Dimension(800, 350));
        backgroundPanel.add(settingsPanel, BorderLayout.NORTH);
        backgroundPanel.add(classSettingsPanel);
        backgroundPanel.add(createButton, BorderLayout.SOUTH);
        frame.add(backgroundPanel);

        frame.setVisible(true);
        frame.setSize(new Dimension(800, 800));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == timeSlider) {
            if (timeSlider2.getValue() < timeSlider.getValue()) {
                timeSlider2.setValue(timeSlider.getValue() + 1);
            }
        } else if (e.getSource() == timeSlider2) {
            if (timeSlider.getValue() > timeSlider2.getValue()) {
                timeSlider.setValue(timeSlider2.getValue() - 1);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            String className = JOptionPane.showInputDialog(null, "Enter the class ID (Format: AAA###).");
            if (isValidName(className)) {
                listModel.add(0, className.toUpperCase());
            } else {
                JOptionPane.showMessageDialog(null, "Enter a valid name", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == removeButton) {
            try {
                listModel.remove(classList.getSelectedIndex());
            } catch (Exception exc) {
                JOptionPane.showMessageDialog(null, "Select an item", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == editButton) {
            if (classList.isSelectionEmpty()) {
                JOptionPane.showMessageDialog(null, "Select an item", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String className = JOptionPane.showInputDialog(null, "What would you like to change this class to?");
            if (className.length() == 0) {
                listModel.remove(classList.getSelectedIndex());
            } else if (!isValidName(className)) {
                JOptionPane.showMessageDialog(null, "Enter a valid name", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                listModel.set(classList.getSelectedIndex(), className);
            }
        } else if (e.getSource() == createButton) {
            createSchedules();
            toggleScreens();
        } else if (e.getSource() == tryAgainButton) {
            toggleScreens();
        } else if (e.getSource() == fall) {
            Scraper.setSemester("fall");
        } else if (e.getSource() == spring) {
            Scraper.setSemester("spring");
        } else if (e.getSource() == summer) {
            Scraper.setSemester("summer");
        } else if (e.getSource() == pecos) {
            Scraper.setCampus("pecos");
        } else if (e.getSource() == notPecos) {
            Scraper.setCampus("williams");
        }
    }

    private boolean isValidName(String className) {
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

    private void createSchedules() {
        Scraper.totalPossibleSchedules = 0;
        try {
            results.setText(
                    Scraper.scrapeValidSchedules(readCourseNumber(), readTimeConstraints(), readCampus(), readYear(),
                            readSemester(), readCourseNames()) + "\nBased on your filters, I displayed "
                            + Scraper.getValidSchedules().size() + " of " + Scraper.totalPossibleSchedules
                            + " possible schedules.\n\n\n");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void toggleScreens() {
        if (backgroundPanel.isShowing()) {
            frame.remove(backgroundPanel);
            frame.add(backgroundPanel2);
        } else {
            frame.remove(backgroundPanel2);
            frame.add(backgroundPanel);
        }
        frame.repaint();
        frame.revalidate();
    }

    private int readCourseNumber() throws Exception {
        if (listModel.size() == 0) {
            throw new Exception("The number of courses is invalid.");
        }
        return listModel.size();
    }

    private int readCampus() {
        return Scraper.getCampus();
    }

    private int[] readTimeConstraints() throws Exception {
        int[] timeConstraints = new int[2];
        timeConstraints[0] = timeSlider.getValue() * 100;
        timeConstraints[1] = timeSlider2.getValue() * 100;
        if (timeConstraints[0] > timeConstraints[1]) {
            throw new Exception("The time constraints are invalid.");
        }
        return timeConstraints;
    }

    private String[] readCourseNames() {
        String[] courseNames = new String[listModel.getSize()];
        for (int i = 0; i < listModel.getSize(); i++) {
            courseNames[i] = listModel.get(i);
        }
        return courseNames;
    }

    private int readSemester() {
        return Scraper.getSemester();
    }

    private int readYear() throws Exception {
        int year = (int) spinner.getValue();
        if (year < 2025 || year > 2100) {
            throw new Exception("The year is invalid.");
        }
        return year;
    }
}
