package org.cubyte.edumon.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import org.cubyte.edumon.client.Main;
import org.cubyte.edumon.client.messaging.messagebody.BreakRequest;

public class OptionsController implements Controller {
    private Main app;
    @FXML
    private Label title;
    @FXML
    private Label room;
    @FXML
    private Label server;
    @FXML
    private Label name;
    @FXML
    private Label seat;
    @FXML
    private Label sent;
    @FXML
    private Label received;
    @FXML
    private Separator separator;
    @FXML
    private Button sendBreakRequest;
    @FXML
    private Button logout;
    @FXML
    private CheckBox keySensor;
    @FXML
    private CheckBox mouseSensor;
    @FXML
    private CheckBox micSensor;

    @Override
    public void setApp(Main app) {
        this.app = app;
    }

    @FXML
    private void handleLogout() {
        app.resetToLogin();
        app.unregisterSensorListeners();
        app.killExecutors();
    }

    @FXML
    private void handleSendBreakrequest() {
        app.getQueue().queue(app.getFactory().create(new BreakRequest()));
        app.getNotificationSystem().showBreakRequestConfirm();
    }

    @FXML
    private void handleCloseApp() {
        app.exit();
    }
    @FXML
    private void handleSendKeyData() {
        app.setSendKeyData(keySensor.isSelected());
    }
    @FXML
    private void handleSendMouseData() {
        app.setSendMouseData(mouseSensor.isSelected());
    }
    @FXML
    private void handleSendMicData() {
        app.setSendMicData(micSensor.isSelected());
    }

    public OptionsController sendKeyData(boolean sendKeyData) {
        keySensor.setSelected(sendKeyData);
        return this;
    }
    public OptionsController sendMouseData(boolean sendMouseData) {
        mouseSensor.setSelected(sendMouseData);
        return this;
    }
    public OptionsController sendMicData(boolean sendMicData) {
        micSensor.setSelected(sendMicData);
        return this;
    }

    public void setDataOverview() {
        room.setText("Raum " + app.getRoom());
        server.setText("Server: " + app.getServer());
        name.setText(app.getName());
        seat.setText("Reihe " + app.getSeat().y + " Sitzplatz " + app.getSeat().x);
    }

    public void setOptions(boolean isOptions) {
        sendBreakRequest.setVisible(!isOptions);
        logout.setVisible(!isOptions);
        separator.setVisible(!isOptions);
        if (isOptions) {
            title.setText("Optionen");
        } else {
            title.setText("EduMon Client");
        }
    }

    public void setSent(int n) {
        sent.setText(n + " gesendet");
    }
    public void setReceived(int n) {
        received.setText(n + " empfangen");
    }
}
