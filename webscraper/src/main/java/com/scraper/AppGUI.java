package com.scraper;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AppGUI implements ActionListener {
    private JFrame frame;
    private JPanel backgroundPanel, settingsPanel, classSettingsPanel, buttonPanel,
            backgroundPanel2;
    private JPanel classSettingsLeftPanel, classSettingsRightPanel;
    private JList<String> classList;
    private DefaultListModel<String> listModel;
    private JTextField numberOfClassesField, yearField;
    private JRadioButton pecos, notPecos, fall, spring, summer;
    private ButtonGroup pecosOrNot, semester;
    private JSlider timeSlider, timeSlider2;
    private JButton addButton, removeButton, editButton, createButton;
    private final int numberOfSettings = 4;

    public AppGUI() {
        frame = new JFrame("Schedule Creator");
        backgroundPanel = new JPanel(new GridLayout(3, 1));
        backgroundPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        settingsPanel = new JPanel(new GridLayout((int) Math.ceil(numberOfSettings / 2.0), 2));
        buttonPanel = new JPanel();
        classList = new JList<>();
        numberOfClassesField = new JTextField();
        yearField = new JTextField();
        timeSlider = new JSlider();
        timeSlider2 = new JSlider();
        listModel = new DefaultListModel<>();

        {
            classSettingsPanel = new JPanel(new GridLayout(1, 2));
            classSettingsLeftPanel = new JPanel(new GridLayout(2, 1));
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
            classSettingsLeftPanel.add(new JLabel("Classes"));
            classSettingsLeftPanel.add(classList);
            classSettingsPanel.add(classSettingsLeftPanel);
            classSettingsPanel.add(classSettingsRightPanel);
        }

        createButton = new JButton("Create");

        {
            JPanel[] panelsInSettings = new JPanel[numberOfSettings];
            for (int i = 0; i < numberOfSettings; i++) {
                panelsInSettings[i] = new JPanel(new GridLayout(2, 1));
            }
            JLabel[] labels = new JLabel[numberOfSettings];
            labels[0] = new JLabel("Year");
            labels[1] = new JLabel("Time");
            labels[2] = new JLabel("Semester");
            labels[3] = new JLabel("Campus");
            pecos = new JRadioButton("Pecos");
            notPecos = new JRadioButton("Pecos & Williams");
            fall = new JRadioButton("Fall");
            spring = new JRadioButton("Spring");
            summer = new JRadioButton("Summer");
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
            JPanel sliderHolder = new JPanel(new GridLayout(2,1));
            sliderHolder.add(timeSlider);
            sliderHolder.add(timeSlider2);
            JComponent[] doodads = new JComponent[numberOfSettings];
            doodads[0] = new JTextField(5);
            doodads[1] = sliderHolder;
            doodads[2] = semesterRadioPanel;
            doodads[3] = campusRadioPanel;
            for (int i = 0; i < numberOfSettings; i++) {
                panelsInSettings[i].add(labels[i]);
                panelsInSettings[i].add(doodads[i]);
                settingsPanel.add(panelsInSettings[i]);
            }
        }

        frame.add(backgroundPanel);
        backgroundPanel.add(settingsPanel);
        backgroundPanel.add(classSettingsPanel);
        backgroundPanel.add(createButton);

        frame.setVisible(true);
        frame.setSize(new Dimension(500, 500));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {

        } else if (e.getSource() == removeButton) {

        } else if (e.getSource() == editButton) {

        } else if (e.getSource() == createButton) {
            readUserInputs();
        }
    }

    private void readUserInputs() {
        int[] timeConstraints = new int[] {timeSlider.getValue(), timeSlider2.getValue()};
        
    }
}
