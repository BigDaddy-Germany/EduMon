package org.cubyte.edumon.client.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.cubyte.edumon.client.Main;
import org.cubyte.edumon.client.messaging.messagebody.util.Dimensions;
import org.cubyte.edumon.client.messaging.messagebody.util.Position;

import static javafx.scene.input.KeyCode.ESCAPE;
import static org.cubyte.edumon.client.Scene.LOGIN_CONFIRM;
import static org.cubyte.edumon.client.Scene.NAME_CHOOSER;

public class SeatChooserController implements Controller {
    private static final String TABLE_IMAGE = "/table.png";
    private static final Image tableImage;

    private Main app;
    @FXML
    private Pane pane;
    @FXML
    private Label roomAndName;
    @FXML
    private GridPane seatingplan;

    static {
        tableImage = new Image(SeatChooserController.class.getResourceAsStream(TABLE_IMAGE));
    }

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
        System.out.println("Back in seatchooser view.");
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
                double columnWidth = 573d / dimensions.width;
                if (columnWidth > 130) {
                    columnWidth = 130;
                }
                seatingplan.setPrefWidth(columnWidth * dimensions.width);
                double rowHeight = 260d / dimensions.height;
                if (rowHeight > 75) {
                    rowHeight = 75;
                }
                seatingplan.setMinHeight(rowHeight * dimensions.height);
                Position seat = app.getSeat();

                for (int x = 0; x < dimensions.width; x++) {
                    for (int y = 0; y < dimensions.height; y++) {
                        final int seatX = dimensions.width - x;
                        final int seatY = y + 1;

                        final Label text = new Label(app.getName());
                        text.setMinWidth(columnWidth);
                        text.setMinHeight(rowHeight - (rowHeight / 4d));
                        text.setStyle("-fx-alignment: top-center");
                        text.setVisible(false);

                        final Hyperlink link = new Hyperlink();
                        link.setPrefSize(columnWidth, rowHeight);
                        link.focusedProperty().addListener(new ChangeListener<Boolean>() {
                            @Override
                            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                                if (newValue) {
                                    text.setVisible(true);
                                } else {
                                    text.setVisible(false);
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
                                app.changeScene(LOGIN_CONFIRM);
                                ((LoginConfirmController) LOGIN_CONFIRM.getController()).waitForConfirmation();
                            }
                        });

                        final ImageView image = new ImageView(tableImage);
                        image.setFitWidth(Math.ceil(columnWidth));
                        image.setFitHeight(Math.ceil(rowHeight));

                        seatingplan.add(image, x, y);
                        seatingplan.add(text, x, y);
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
