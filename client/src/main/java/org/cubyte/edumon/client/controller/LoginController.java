package org.cubyte.edumon.client.controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import org.cubyte.edumon.client.Main;
import org.cubyte.edumon.client.eventsystem.Victim;
import org.cubyte.edumon.client.messaging.Message;
import org.cubyte.edumon.client.messaging.messagebody.NameList;

import static org.cubyte.edumon.client.Main.Scene.*;
import static org.cubyte.edumon.client.Main.Scene.LOADING;

public class LoginController implements Victim<Message>, Controller {
    @FXML
    private TextField room;
    private Main app;

    public LoginController() {
    }

    public void setApp(Main app) {
        this.app = app;

        room.setText(app.getRoom());
    }

    public String getRoom() {
        return room.getText();
    }

    @FXML
    private void handleNext() {
        app.setRoom(room.getText());
        //app.setServer();
        app.getQueue().aimAt(NameList.class, this);
        app.changeScene(LOADING);
        app.getQueue().send();
        app.getQueue().execute();
    }
    @FXML
    private void handleClose() {
        app.exit();
    }

    @Override
    public void take(Message bullet) {
        //TODO what is when no namelist is on the server?
        app.changeScene(NAME_CHOOSER);
    }
}