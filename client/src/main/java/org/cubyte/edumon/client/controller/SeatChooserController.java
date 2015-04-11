package org.cubyte.edumon.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.cubyte.edumon.client.Main;

import static org.cubyte.edumon.client.Main.Scene.NAME_CHOOSER;

public class SeatChooserController implements Controller {
    private Main app;
    @FXML
    private Label roomAndName;

    @FXML
    private void handleBack() {
        app.changeScene(NAME_CHOOSER);
    }

    @Override
    public void setApp(Main app) {
        this.app = app;
    }

    public void setRoomAndName(String room, String name) {
        this.roomAndName.setText("Raum " + room + " | Name: " + name);
    }
}
