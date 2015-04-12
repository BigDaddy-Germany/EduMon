package org.cubyte.edumon.client.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.cubyte.edumon.client.Main;

import java.util.ArrayList;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;
import static org.cubyte.edumon.client.Main.Scene.LOGIN;
import static org.cubyte.edumon.client.Main.Scene.SEAT_CHOOSER;

public class NameChooserController implements Controller {
    private Main app;
    private String name;
    @FXML
    private Pane pane;
    @FXML
    private ListView nameList;
    @FXML
    private Label room;

    @FXML
    private void initialize() {
        nameList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                handleNext();
            }
        });
        nameList.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                KeyCode code = keyEvent.getCode();
                if(code == ENTER) {
                    handleNext();
                } else if (code == ESCAPE) {
                    handleBack();
                }
            }
        });
        pane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == ESCAPE) {
                    handleBack();
                }
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

    private void handleNext() {
        name = (String) nameList.getSelectionModel().getSelectedItem();
        ((SeatChooserController) SEAT_CHOOSER.getController()).setRoomAndName(app.getRoom(), name).setDimensions();
        app.changeScene(SEAT_CHOOSER);
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
