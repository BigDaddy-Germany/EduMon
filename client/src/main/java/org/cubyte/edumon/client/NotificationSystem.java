package org.cubyte.edumon.client;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.cubyte.edumon.client.controller.BreakRequestConfirmController;
import org.cubyte.edumon.client.controller.ThumbfeedbackController;
import org.cubyte.edumon.client.eventsystem.Victim;
import org.cubyte.edumon.client.messaging.Message;
import org.cubyte.edumon.client.messaging.messagebody.ThumbFeedback;
import org.cubyte.edumon.client.messaging.messagebody.ThumbRequest;

import java.awt.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.cubyte.edumon.client.Scene.BREAK_REQUEST_CONFIRM;
import static org.cubyte.edumon.client.Scene.THUMBFEEDBACK;

public class NotificationSystem implements Victim<Message> {
    private Stage stage;
    private Main app;
    private ScheduledFuture<?> hideTimer;

    public NotificationSystem(final Main app) {
        this.app = app;
        app.getQueue().aimAt(ThumbRequest.class, this);
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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.setScene(BREAK_REQUEST_CONFIRM.getScene());
                stage.show();
                setHideTimer();
            }
        });
    }

    @Override
    public void take(Message bullet) {
        if (bullet.body.getClass().isAssignableFrom(ThumbRequest.class)) {
            final ThumbRequest body = ((ThumbRequest) bullet.body);
            if (body.type == ThumbRequest.FeedbackType.thumb) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        cancelHideTimer();
                        stage.setScene(THUMBFEEDBACK.getScene());
                        stage.show();
                        ((ThumbfeedbackController) THUMBFEEDBACK.getController()).setId(body.id).resetThumb();
                    }
                });
            }
        }
    }

    private void setHideTimer() {
        cancelHideTimer();
        hideTimer = app.getScheduledExecutorService().schedule(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        stage.hide();
                    }
                });
            }
        }, 3, TimeUnit.SECONDS);
    }

    private void cancelHideTimer() {
        if (hideTimer != null) {
            hideTimer.cancel(true);
        }
    }

    public void hideNotification() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.hide();
            }
        });
    }
}
