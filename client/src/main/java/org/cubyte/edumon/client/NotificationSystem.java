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
    private boolean showing;

    public NotificationSystem(final Main app) {
        this.app = app;
        app.getQueue().aimAt(ThumbRequest.class, this);

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        littleY = screenSize.getHeight() - 170;
        bigY = screenSize.getHeight() - 290;

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage = new Stage();
                stage.initStyle(StageStyle.UNDECORATED);
                stage.setX(screenSize.getWidth() - 320);
                stage.setY(littleY);
                stage.getIcons().add(app.getAppIcon());
                stage.setScene(THUMBFEEDBACK.getScene());

                confirmStage = new Stage();
                confirmStage.initStyle(StageStyle.UNDECORATED);
                confirmStage.setX(screenSize.getWidth() - 320);
                confirmStage.getIcons().add(app.getAppIcon());
                confirmStage.setScene(BREAK_REQUEST_CONFIRM.getScene());
            }
        });
        this.enabled = false;
        this.showing = false;
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
                        stage.hide();
                        stage.show();
                        showing = true;
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

    public boolean hideNotification() {
        if (!showing) {
            return false;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.hide();
                showing = false;
                if (confirmStage.isShowing()) {
                    confirmStage.setY(littleY);
                }
            }
        });
        return true;
    }

    public void enable() {
        enabled = true;
    }
    public void disable() {
        enabled = false;
    }
}
