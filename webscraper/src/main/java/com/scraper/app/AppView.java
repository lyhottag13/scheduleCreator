package com.scraper.app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;

public class AppView {
    private JFrame frame;
    private JPanel backgroundPanel, settingsPanel, classSettingsPanel,
            backgroundPanel2, mainPanel, choicePanel;
    private JPanel classSettingsLeftPanel, classSettingsRightPanel;
    private JPanel semesterRadioPanel, campusRadioPanel;
    private JPanel tablesPanel;

    private JScrollPane scrollPane;

    private JList<String> classList;
    private DefaultListModel<String> listModel;
    private JRadioButton pecos, pecosAndWilliams;
    private JRadioButton[] semesterButtons;
    private ButtonGroup campusButtonGroup, semesterButtonGroup;

    private JPanel[] sliderPanels;
    private JSlider[] timeSliders;


    private JButton addButton, removeButton, editButton, createButton, tryAgainButton;
    private JTextField identificationInput;
    private DefaultComboBoxModel<String> boxModel;
    private JComboBox<String> comboBox;
    private JTable[] tables;
    private final int NUMBER_OF_SETTINGS = 3;
    public static final int FRAME_HEIGHT = 700;
    public static final int FRAME_WIDTH = 1000;
    public static final int BORDER_SIZE = 20;

    public AppView() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Schedule Creator");
        mainPanel = new JPanel(new CardLayout());
        backgroundPanel = new JPanel(new BorderLayout(0, 50));
        backgroundPanel.setBorder(new EmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
        settingsPanel = new JPanel(new GridLayout((int) Math.ceil(NUMBER_OF_SETTINGS / 2.0), 2, 50, 10));
        settingsPanel.setBorder(new TitledBorder("Settings"));
        // Creates the panel with the class settings.
        createClassSettings();

        // Creates the regular settings.
        createRegularSettings();

        // Panel that shows when "Add" is clicked.
        createAddPanel();

        // Screen 2, the one that shows the results.
        createResultsScreen();

        createButton = new JButton("Create");

        settingsPanel.setPreferredSize(new Dimension(800, 350));
        backgroundPanel.add(settingsPanel, BorderLayout.NORTH);
        backgroundPanel.add(classSettingsPanel);
        backgroundPanel.add(createButton, BorderLayout.SOUTH);
        frame.add(mainPanel);
        mainPanel.add(backgroundPanel, "panel1");
        mainPanel.add(backgroundPanel2, "panel2");


        frame.setVisible(true);
        frame.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void createResultsScreen() {
        JPanel tryAgainButtonPanel = new JPanel();
        tryAgainButton = new JButton("Try Again?") {{
            setPreferredSize(new Dimension(FRAME_WIDTH / 3, FRAME_HEIGHT / 20));
        }};
        tryAgainButtonPanel.add(tryAgainButton);

        scrollPane = new JScrollPane();
        scrollPane.setViewportView(tablesPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        backgroundPanel2 = new JPanel(new BorderLayout(20, 20));
        backgroundPanel2.setBorder(new LineBorder(new Color(0xFFFFFF), BORDER_SIZE));
        backgroundPanel2.add(new JLabel("Possible Schedules"));
        backgroundPanel2.add(scrollPane);
        backgroundPanel2.add(tryAgainButtonPanel, BorderLayout.SOUTH);
    }

    private void createRegularSettings() {
        JPanel[] panelsInSettings = new JPanel[NUMBER_OF_SETTINGS];
        LineBorder border = new LineBorder(new Color(0, 0, 0));
        panelsInSettings[0] = new JPanel(new BorderLayout());
        panelsInSettings[0].setBorder(new TitledBorder(border, "Time (Military Standard)"));
        panelsInSettings[1] = new JPanel(new BorderLayout());
        panelsInSettings[1].setBorder(new TitledBorder(border, "Semester"));
        panelsInSettings[2] = new JPanel(new BorderLayout());
        panelsInSettings[2].setBorder(new TitledBorder(border, "Campus"));

        // Radio Buttons.
        semesterButtonGroup = new ButtonGroup();
        semesterRadioPanel = new JPanel();
        semesterButtons = new JRadioButton[3];
        for (int i = 0; i < 3; i++) {
            semesterButtons[i] = new JRadioButton();
            semesterRadioPanel.add(semesterButtons[i]);
            semesterButtonGroup.add(semesterButtons[i]);
        }
        pecos = new JRadioButton("Pecos");
        pecosAndWilliams = new JRadioButton("Pecos & Williams");
        campusButtonGroup = new ButtonGroup();
        campusRadioPanel = new JPanel();
        campusButtonGroup.add(pecos);
        campusButtonGroup.add(pecosAndWilliams);
        campusRadioPanel.add(pecos);
        campusRadioPanel.add(pecosAndWilliams);
        semesterButtons[0].setSelected(true);
        pecos.setSelected(true);

        JPanel sliderHolder = createSliders();

        // This adds the components and their labels together.
        JComponent[] doodads = new JComponent[NUMBER_OF_SETTINGS];
        doodads[0] = sliderHolder;
        doodads[1] = semesterRadioPanel;
        doodads[2] = campusRadioPanel;
        for (int i = 0; i < NUMBER_OF_SETTINGS; i++) {
            panelsInSettings[i].add(doodads[i], BorderLayout.CENTER);
            settingsPanel.add(panelsInSettings[i]);
        }
    }

    private JPanel createSliders() {
        // Sliders
        final int EARLIEST_TIME = 6;
        final int LATEST_TIME = 22;
        timeSliders = new JSlider[]{new JSlider(EARLIEST_TIME, LATEST_TIME, EARLIEST_TIME), new JSlider(EARLIEST_TIME, LATEST_TIME, LATEST_TIME)};
        sliderPanels = new JPanel[]{new JPanel(new BorderLayout()) {{
            add(new JLabel("Start Time:"), BorderLayout.WEST);
        }}, new JPanel(new BorderLayout()) {{
            add(new JLabel("End Time:"), BorderLayout.WEST);
        }}};
        JPanel sliderHolder = new JPanel(new GridLayout(2, 1));
        for (int i = 0; i < timeSliders.length; i++) {
            timeSliders[i].setPaintTicks(true);
            timeSliders[i].setMajorTickSpacing(1);
            timeSliders[i].setPaintLabels(true);
            timeSliders[i].setSnapToTicks(true);
            timeSliders[i].setPaintTrack(false);
            sliderPanels[i].add(timeSliders[i]);
            sliderHolder.add(sliderPanels[i]);
        }
        return sliderHolder;
    }

    private void createClassSettings() {
        classSettingsPanel = new JPanel(new GridLayout(1, 2, 10, 50));
        classSettingsPanel.setBorder(new TitledBorder("Classes"));
        classSettingsLeftPanel = new JPanel(new BorderLayout());
        classSettingsRightPanel = new JPanel(new GridLayout(3, 1));

        addButton = new JButton("Add");
        removeButton = new JButton("Remove");
        editButton = new JButton("Edit");
        listModel = new DefaultListModel<>();
        classList = new JList<>(listModel);
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
        timeSliders[0].addChangeListener(listener);
    }

    public void setTimeSlider2Listener(ChangeListener listener) {
        timeSliders[1].addChangeListener(listener);
    }

    public void setCreateButtonListener(ActionListener listener) {
        createButton.addActionListener(listener);
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

    public JSlider getTimeSlider() {
        return timeSliders[0];
    }

    public JSlider getTimeSlider2() {
        return timeSliders[1];
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

    public ButtonGroup getSemesterButtonGroup() {
        return semesterButtonGroup;
    }

    public JPanel getSemesterRadioPanel() {
        return semesterRadioPanel;
    }

    public JRadioButton[] getSemesterRadioButtons() {
        return semesterButtons;
    }

    public JTable[] getTables() {
        return tables;
    }

    public void setTablesSize(int numberOfTables) {
        tables = new JTable[numberOfTables];
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JPanel getTablesPanel() {
        return tablesPanel;
    }

    public void setTablesPanel(JPanel panel) {
        tablesPanel = panel;
        scrollPane.setViewportView(tablesPanel);
    }

    public JPanel getBackgroundPanel2() {
        return backgroundPanel2;
    }


    public int getNumberOfCourses() {
        return listModel.size();
    }

    public int[] getTimeConstraints() throws Exception {
        int[] timeConstraints = new int[2];
        timeConstraints[0] = timeSliders[0].getValue() * 100;
        timeConstraints[1] = timeSliders[1].getValue() * 100;
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
}
