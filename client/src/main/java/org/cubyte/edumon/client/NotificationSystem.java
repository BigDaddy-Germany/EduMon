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
    private final static int HIDE_TIMER = 3;
    private Stage stage;
    private Stage confirmStage;
    private Main app;
    private ScheduledFuture<?> hideTimer;
    private boolean enabled;
    private final double littleY; // y pos of confirmStage when stage is not shown
    private final double bigY; // y pos of confirmStage when stage is shown

    public NotificationSystem(final Main app) {
        this.app = app;
        app.getQueue().aimAt(ThumbRequest.class, this);

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        littleY = screenSize.getHeight() - 120;
        bigY = screenSize.getHeight() - 240;

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                BREAK_REQUEST_CONFIRM.getScene().setFill(null);
                THUMBFEEDBACK.getScene().setFill(null);

                stage = new Stage();
                stage.initStyle(StageStyle.UNDECORATED);
                stage.setX(screenSize.getWidth() - 320);
                stage.setY(littleY);
                stage.getIcons().add(app.getAppIcon());

                confirmStage = new Stage();
                confirmStage.initStyle(StageStyle.UNDECORATED);
                confirmStage.setX(screenSize.getWidth() - 320);
                confirmStage.getIcons().add(app.getAppIcon());
            }
        });
        this.enabled = false;
    }

    public void showBreakRequestConfirm() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                confirmStage.setX(stage.getX());
                if (stage.isShowing()) {
                    confirmStage.setY(bigY);
                } else {
                    confirmStage.setY(littleY);
                }
                confirmStage.setScene(BREAK_REQUEST_CONFIRM.getScene());
                confirmStage.show();
                setHideTimer();
            }
        });
    }

    @Override
    public void take(Message bullet) {
        if (!enabled) {
            return;
        }
        if (bullet.body.getClass().isAssignableFrom(ThumbRequest.class)) {
            final ThumbRequest body = ((ThumbRequest) bullet.body);
            if (body.type == ThumbRequest.FeedbackType.thumb ||body.type == ThumbRequest.FeedbackType.rating) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (confirmStage.isShowing()) {
                            confirmStage.setY(bigY);
                        }
                        stage.setScene(THUMBFEEDBACK.getScene());
                        stage.show();
                        ((ThumbfeedbackController) THUMBFEEDBACK.getController()).setId(body.id).setFeedbackType(body.type).resetThumb();
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
                        confirmStage.hide();
                    }
                });
            }
        }, HIDE_TIMER, TimeUnit.SECONDS);
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
                if (confirmStage.isShowing()) {
                    confirmStage.setY(littleY);
                }
            }
        });
    }

    public void enable() {
        enabled = true;
    }
    public void disable() {
        enabled = false;
    }
}
