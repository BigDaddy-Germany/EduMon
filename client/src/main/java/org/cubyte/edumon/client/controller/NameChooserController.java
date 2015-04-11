package org.cubyte.edumon.client.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.cubyte.edumon.client.Main;

import java.util.ArrayList;

import static org.cubyte.edumon.client.Main.Scene.LOGIN;
import static org.cubyte.edumon.client.Main.Scene.SEAT_CHOOSER;

public class NameChooserController implements Controller {
    private Main app;
    private String name;
    @FXML
    private ListView nameList;
    @FXML
    private Label room;

    @FXML
    private void initialize() {
        nameList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                name = newValue;
                ((SeatChooserController) SEAT_CHOOSER.getController()).setRoomAndName(app.getRoom(), name).setDimensions();
                app.changeScene(SEAT_CHOOSER);
            }
        });
    }

    @Override
    public void setApp(Main app) {
        this.app = app;
    }

    @FXML
    private void handleBack() {
        app.changeScene(LOGIN);
    }

    public void setNameList(ArrayList<String> nameList) {
        this.nameList.setItems(FXCollections.observableArrayList(nameList));
    }

    public String getName() {
        return name;
    }

    public void setRoom(String room) {
        this.room.setText("Raum " + room);
    }
}
