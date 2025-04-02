package com.scraper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
            model.readWebsite(view);
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
        String userInput = view.getComboBoxModel().getElementAt(view.getComboBox().getSelectedIndex()) + view.getIdentificationInput().getText();
        if (!model.isValidName(userInput)) {
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
            view.getResults().setText(
                    AppModel.createValidScheduleString(view.getNumberOfCourses(), view.getTimeConstraints(), model.getSemesterValue(), view.getCourseNames()) + "\nBased on your filters, I displayed "
                            + AppModel.getValidSchedules().size() + " of " + AppModel.totalPossibleSchedules
                            + " possible schedules.\n\n\n");
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private void toggleScreen() {
        ((CardLayout) view.getMainPanel().getLayout()).next(view.getMainPanel());
    }
}
