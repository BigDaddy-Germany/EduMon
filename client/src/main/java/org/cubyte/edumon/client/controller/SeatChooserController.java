package org.cubyte.edumon.client.controller;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.cubyte.edumon.client.Main;
import org.cubyte.edumon.client.messaging.messagebody.WhoAmI;
import org.cubyte.edumon.client.messaging.messagebody.util.Dimensions;
import org.cubyte.edumon.client.messaging.messagebody.util.Position;

import static org.cubyte.edumon.client.Main.Scene.LOGIN_CONFIRM;
import static org.cubyte.edumon.client.Main.Scene.NAME_CHOOSER;

public class SeatChooserController implements Controller {
    private Main app;
    private String name;
    private Dimensions dimensions;
    @FXML
    private Label roomAndName;
    @FXML
    private GridPane seatingplan;

    @FXML
    private void handleBack() {
        app.changeScene(NAME_CHOOSER);
    }

    @Override
    public void setApp(Main app) {
        this.app = app;
    }

    public SeatChooserController setRoomAndName(String room, String name) {
        this.name = name;
        this.roomAndName.setText("Raum " + room + " | Name: " + name);
        return this;
    }

    public void storeDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public void setDimensions() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                seatingplan.getChildren().removeAll(seatingplan.getChildren());
                double rowHeight = 300d / dimensions.height;
                double columnWidth = 573d / dimensions.width;
                for(int x = 0; x < dimensions.width; x++) {
                    for(int y = 0; y < dimensions.height; y++) {
                        final Hyperlink link = new Hyperlink(name);
                        final int seatX = x; final int seatY = y + 1;
                        link.setPrefSize(columnWidth, rowHeight);
                        link.setAlignment(Pos.CENTER);
                        link.setStyle("-fx-text-fill: #fff;");
                        link.setOnMouseEntered(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {
                                link.setStyle("-fx-text-fill: #000;");
                            }
                        });
                        link.setOnMouseExited(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {
                                link.setStyle("-fx-text-fill: #fff;");
                            }
                        });
                        link.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {
                                app.getQueue().queue(app.getFactory().create(new WhoAmI(name, new Position(dimensions.width - seatX, seatY))));
                                app.getQueue().send();
                                app.changeScene(LOGIN_CONFIRM);
                                ((LoginConfirmController) LOGIN_CONFIRM.getController()).confirmLogin();
                                app.startBackgroundExecution();
                            }
                        });
                        seatingplan.add(link, x, y);
                    }
                }
            }
        });
    }
}
