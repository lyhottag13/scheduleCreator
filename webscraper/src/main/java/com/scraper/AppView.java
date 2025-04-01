package com.scraper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.time.Year;

public class AppView {
    private JFrame frame;
    private JPanel backgroundPanel, settingsPanel, classSettingsPanel,
            backgroundPanel2, mainPanel, choicePanel;
    private JPanel classSettingsLeftPanel, classSettingsRightPanel;
    private JScrollPane scrollPane;
    private JList<String> classList;
    private DefaultListModel<String> listModel;
    private JRadioButton pecos, pecosAndWilliams, fall, spring, summer;
    private ButtonGroup campusButtonGroup, semesterButtonGroup;
    private JSlider timeSlider, timeSlider2;
    private JButton addButton, removeButton, editButton, createButton, tryAgainButton;
    private JTextPane results;
    private JTextField identificationInput;
    private JSpinner spinner;
    private DefaultComboBoxModel<String> boxModel;
    private JComboBox<String> comboBox;
    private final int NUMBER_OF_SETTINGS = 4;

    public AppView() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Schedule Creator");
        mainPanel = new JPanel(new CardLayout());
        backgroundPanel = new JPanel(new BorderLayout(0, 50));
        backgroundPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        settingsPanel = new JPanel(new GridLayout((int) Math.ceil(NUMBER_OF_SETTINGS / 2.0), 2, 50, 10));
        settingsPanel.setBorder(new TitledBorder("Settings"));
        listModel = new DefaultListModel<>();
        classList = new JList<>(listModel);
        final int EARLIEST_TIME = 6;
        final int LATEST_TIME = 22;
        timeSlider = new JSlider(EARLIEST_TIME, LATEST_TIME, EARLIEST_TIME);
        timeSlider2 = new JSlider(EARLIEST_TIME, LATEST_TIME, LATEST_TIME);
        // Class Settings.
        createClassSettings();

        createButton = new JButton("Create");
        // Regular Settings
        createRegularSettings();

        // Screen 2, the one that shows the results.
        createResultsScreen();

        createAddPanel();

        settingsPanel.setPreferredSize(new Dimension(800, 350));
        backgroundPanel.add(settingsPanel, BorderLayout.NORTH);
        backgroundPanel.add(classSettingsPanel);
        backgroundPanel.add(createButton, BorderLayout.SOUTH);
        frame.add(mainPanel);
        mainPanel.add(backgroundPanel, "panel1");
        mainPanel.add(backgroundPanel2, "panel2");

        frame.setVisible(true);
        frame.setSize(new Dimension(800, 800));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void createResultsScreen() {
        tryAgainButton = new JButton("Try Again?");

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

    private void createRegularSettings() {
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
        pecosAndWilliams = new JRadioButton("Pecos & Williams");
        fall = new JRadioButton("Fall");
        spring = new JRadioButton("Spring");
        summer = new JRadioButton("Summer");
        campusButtonGroup = new ButtonGroup();
        semesterButtonGroup = new ButtonGroup();
        JPanel campusRadioPanel = new JPanel();
        campusButtonGroup.add(pecos);
        campusButtonGroup.add(pecosAndWilliams);
        campusRadioPanel.add(pecos);
        campusRadioPanel.add(pecosAndWilliams);
        JPanel semesterRadioPanel = new JPanel();
        semesterButtonGroup.add(fall);
        semesterButtonGroup.add(spring);
        semesterButtonGroup.add(summer);
        semesterRadioPanel.add(fall);
        semesterRadioPanel.add(spring);
        semesterRadioPanel.add(summer);
        fall.setSelected(true);
        pecos.setSelected(true);
        AppModel.setCampus("pecos");
        AppModel.setSemester("fall");

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

    private void createClassSettings() {
        classSettingsPanel = new JPanel(new GridLayout(1, 2, 10, 50));
        classSettingsPanel.setBorder(new TitledBorder("Classes"));
        classSettingsLeftPanel = new JPanel(new BorderLayout());
        classSettingsRightPanel = new JPanel(new GridLayout(3, 1));
        addButton = new JButton("Add");
        removeButton = new JButton("Remove");
        editButton = new JButton("Edit");
        boxModel = new DefaultComboBoxModel<>();
        comboBox = new JComboBox<>(boxModel);
        classSettingsRightPanel.add(addButton);
        classSettingsRightPanel.add(removeButton);
        classSettingsRightPanel.add(editButton);
        classSettingsLeftPanel.add(classList);
        classSettingsPanel.add(classSettingsLeftPanel);
        classSettingsPanel.add(classSettingsRightPanel);
    }

    private void createAddPanel() {
        choicePanel = new JPanel();
        identificationInput = new JTextField(3);
        choicePanel.add(comboBox);
        choicePanel.add(identificationInput);
    }

    public void setIdentificationInputKeyAdapter(KeyAdapter adapter) {
        identificationInput.addKeyListener(adapter);
    }

    public void setAddButtonListener(ActionListener listener) {
        addButton.addActionListener(listener);
    }

    public void setRemoveButtonListener(ActionListener listener) {
        removeButton.addActionListener(listener);
    }

    public void setEditButtonListener(ActionListener listener) {
        editButton.addActionListener(listener);
    }

    public void setTryAgainButtonListener(ActionListener listener) {
        tryAgainButton.addActionListener(listener);
    }

    public void setTimeSliderListener(ChangeListener listener) {
        timeSlider.addChangeListener(listener);
    }

    public void setTimeSlider2Listener(ChangeListener listener) {
        timeSlider2.addChangeListener(listener);
    }

    public void setCreateButtonListener(ActionListener listener) {
        createButton.addActionListener(listener);
    }

    public void setFallButtonListener(ActionListener listener) {
        fall.addActionListener(listener);
    }

    public void setSpringButtonListener(ActionListener listener) {
        spring.addActionListener(listener);
    }

    public void setSummerButtonListener(ActionListener listener) {
        summer.addActionListener(listener);
    }

    public void setPecosButtonListener(ActionListener listener) {
        pecos.addActionListener(listener);
    }

    public void setPecosAndWilliamsButtonListener(ActionListener listener) {
        pecosAndWilliams.addActionListener(listener);
    }

    public DefaultListModel<String> getListModel() {
        return listModel;
    }

    public JList<String> getClassList() {
        return classList;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JTextPane getResults() {
        return results;
    }

    public JSlider getTimeSlider() {
        return timeSlider;
    }

    public JSlider getTimeSlider2() {
        return timeSlider2;
    }

    public DefaultComboBoxModel<String> getComboBoxModel() {
        return boxModel;
    }

    public JComboBox<String> getComboBox() {
        return comboBox;
    }

    public JPanel getChoicePanel() {
        return choicePanel;
    }

    public JTextField getIdentificationInput() {
        return identificationInput;
    }

    public int getNumberOfCourses() throws Exception {
        if (listModel.isEmpty()) {
            throw new Exception("The number of courses is invalid.");
        }
        return listModel.size();
    }

    public int getSemesterButtonGroup() {
        return AppModel.getSemester();
    }

    public int[] getTimeConstraints() throws Exception {
        int[] timeConstraints = new int[2];
        timeConstraints[0] = timeSlider.getValue() * 100;
        timeConstraints[1] = timeSlider2.getValue() * 100;
        if (timeConstraints[0] > timeConstraints[1]) {
            throw new Exception("The time constraints are invalid.");
        }
        return timeConstraints;
    }

    public String[] getCourseNames() {
        String[] courseNames = new String[listModel.getSize()];
        for (int i = 0; i < listModel.getSize(); i++) {
            courseNames[i] = listModel.get(i);
        }
        return courseNames;
    }

    public int getYear() throws Exception {
        int year = (int) spinner.getValue();
        if (year < 2025 || year > 2100) {
            throw new Exception("The year is invalid.");
        }
        return year;
    }

}
