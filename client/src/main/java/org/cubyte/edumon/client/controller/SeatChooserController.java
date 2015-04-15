package org.cubyte.edumon.client.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.cubyte.edumon.client.Main;
import org.cubyte.edumon.client.messaging.messagebody.WhoAmI;
import org.cubyte.edumon.client.messaging.messagebody.util.Dimensions;
import org.cubyte.edumon.client.messaging.messagebody.util.Position;

import static javafx.scene.input.KeyCode.ESCAPE;
import static org.cubyte.edumon.client.Scene.LOGIN_CONFIRM;
import static org.cubyte.edumon.client.Scene.NAME_CHOOSER;

public class SeatChooserController implements Controller {
    private Main app;
    @FXML
    private Pane pane;
    @FXML
    private Label roomAndName;
    @FXML
    private GridPane seatingplan;

    @FXML
    private void initialize() {
        pane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == ESCAPE) {
                    handleBack();
                }
            }
        });
    }

    @FXML
    private void handleBack() {
        app.changeScene(NAME_CHOOSER);
    }

    @Override
    public void setApp(Main app) {
        this.app = app;
    }

    public SeatChooserController setInfoBar() {
        this.roomAndName.setText("Raum " + app.getRoom() + " | " + app.getName());
        return this;
    }

    public void setDimensions() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final Dimensions dimensions = app.getRoomState().nameList.dimensions;
                seatingplan.getChildren().removeAll(seatingplan.getChildren());
                double rowHeight = 300d / dimensions.height;
                double columnWidth = 573d / dimensions.width;
                Position seat = app.getSeat();
                for (int x = 0; x < dimensions.width; x++) {
                    for (int y = 0; y < dimensions.height; y++) {
                        final Hyperlink link = new Hyperlink(app.getName());
                        final int seatX = dimensions.width - x;
                        final int seatY = y + 1;
                        link.setPrefSize(columnWidth, rowHeight);
                        link.setAlignment(Pos.CENTER);
                        link.setStyle("-fx-text-fill: #fff;");
                        link.focusedProperty().addListener(new ChangeListener<Boolean>() {
                            @Override
                            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                                if (newValue) {
                                    link.setStyle("-fx-text-fill: #000;");
                                } else {
                                    link.setStyle("-fx-text-fill: #fff;");
                                }
                            }
                        });
                        link.setOnMouseMoved(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {
                                link.requestFocus();
                            }
                        });
                        link.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent mouseEvent) {
                                app.setSeat(new Position(seatX, seatY));
                                app.getQueue().queue(app.getFactory().create(new WhoAmI(app.getName(), app.getSeat())));
                                app.getQueue().send();
                                app.changeScene(LOGIN_CONFIRM);
                                ((LoginConfirmController) LOGIN_CONFIRM.getController()).confirmLogin();
                            }
                        });
                        seatingplan.add(link, x, y);
                        if (seat != null && seatX == seat.x && seatY == seat.y) {
                            link.requestFocus();
                        }
                    }
                }
            }
        });
    }
}
