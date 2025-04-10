package com.scraper;

import com.scraper.app.AppController;
import com.scraper.app.AppModel;
import com.scraper.app.AppView;

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
