package com.kerner1000.drrename.event;

import javafx.event.ActionEvent;

public class MainViewButtonGoEvent extends JavaFXActionEvent {

    public MainViewButtonGoEvent(ActionEvent actionEvent) {
        super(actionEvent);
    }

    public ActionEvent getActionEvent() {
        return ((ActionEvent) getSource());
    }
}