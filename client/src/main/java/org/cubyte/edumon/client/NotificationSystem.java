package org.cubyte.edumon.client;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.cubyte.edumon.client.controller.BreakRequestConfirmController;
import org.cubyte.edumon.client.eventsystem.Victim;
import org.cubyte.edumon.client.messaging.Message;

import java.awt.*;

import static org.cubyte.edumon.client.Scene.BREAK_REQUEST_CONFIRM;
import static org.cubyte.edumon.client.Scene.THUMBFEEDBACK;

public class NotificationSystem implements Victim<Message> {
    private Stage stage;

    public NotificationSystem(final Main app) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage = new Stage();
                stage.initStyle(StageStyle.TRANSPARENT);
                BREAK_REQUEST_CONFIRM.getScene().setFill(null);
                THUMBFEEDBACK.getScene().setFill(null);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                stage.setX(screenSize.getWidth() - 350);
                stage.setY(screenSize.getHeight() - 150);
                stage.getIcons().add(app.getAppIcon());
            }
        });
    }

    public void showBreakRequestConfirm() {
        stage.setScene(BREAK_REQUEST_CONFIRM.getScene());
        stage.show();
        ((BreakRequestConfirmController) BREAK_REQUEST_CONFIRM.getController()).show();
    }

    @Override
    public void take(Message bullet) {

    }

    public void hide() {
        stage.hide();
    }
}
