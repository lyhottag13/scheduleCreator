package com.scraper;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppView view = new AppView();
            AppModel model = new AppModel();
            new AppController(view, model);
        });
    }
}
