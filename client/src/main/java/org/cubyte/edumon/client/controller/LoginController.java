package org.cubyte.edumon.client.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.cubyte.edumon.client.Main;

import static javafx.scene.input.KeyCode.ESCAPE;
import static org.cubyte.edumon.client.Scene.*;
import static org.cubyte.edumon.client.Scene.LOADING;

public class LoginController implements Controller {
    private Main app;
    private String serverAddress;
    @FXML
    private Pane pane;
    @FXML
    private TextField room;
    @FXML
    private TextField server;
    @FXML
    private Hyperlink serverLink;
    @FXML
    private Pane popup;
    @FXML
    private Label serverError;

    @FXML
    private void initialize() {
        pane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == ESCAPE) {
                    if (popup.isVisible()) {
                        handleClose();
                    }
                }
            }
        });
    }

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
        ((NameChooserController) NAME_CHOOSER.getController()).setInfoBar();
        LoadingController loadingController = (LoadingController) LOADING.getController();
        loadingController.setInfoBar();
        app.changeScene(LOADING);
        loadingController.getNameList();
    }

    @FXML
    private void handleServerSave() {
        serverAddress = server.getText();
        setServer(serverAddress);
        popup.setVisible(false);
        serverError.setVisible(false);
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

    public void serverNotReachable() {
        serverError.setVisible(true);
    }
}