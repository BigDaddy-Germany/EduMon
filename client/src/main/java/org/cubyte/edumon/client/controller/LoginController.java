package org.cubyte.edumon.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.cubyte.edumon.client.Main;
import org.cubyte.edumon.client.eventsystem.Victim;
import org.cubyte.edumon.client.messaging.Message;
import org.cubyte.edumon.client.messaging.messagebody.NameList;

import static org.cubyte.edumon.client.Main.Scene.*;
import static org.cubyte.edumon.client.Main.Scene.LOADING;

public class LoginController implements Controller {
    private Main app;

    @FXML
    private TextField room;
    @FXML
    private TextField server;
    @FXML
    private Hyperlink serverLink;
    @FXML
    private Pane popup;

    private String serverAddress;

    public void setApp(Main app) {
        this.app = app;

        room.setText(app.getRoom());
        serverAddress = app.getServer();
        setServer(serverAddress);
    }

    @FXML
    private void handleNext() {
        String room = this.room.getText();
        app.setRoom(room);
        app.setServer(serverAddress);
        ((NameChooserController) NAME_CHOOSER.getController()).setRoom(room);
        LoadingController loadingController = (LoadingController) LOADING.getController();
        loadingController.setRoomAndServer(room, serverAddress);
        app.changeScene(LOADING);
        loadingController.getNameList();
    }

    @FXML
    private void handleServerSave() {
        serverAddress = server.getText();
        setServer(serverAddress);
        popup.setVisible(false);
        room.requestFocus();
    }

    @FXML
    private void handleClose() {
        setServer(serverAddress);
        popup.setVisible(false);
        room.requestFocus();
    }

    @FXML
    private void handlePopup() {
        popup.setVisible(true);
        server.requestFocus();
    }

    private void setServer(String server) {
        this.serverLink.setText("Server: " + server);
        this.server.setText(server);
    }
}