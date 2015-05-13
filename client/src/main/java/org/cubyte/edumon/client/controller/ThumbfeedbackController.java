package org.cubyte.edumon.client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.cubyte.edumon.client.Main;
import org.cubyte.edumon.client.messaging.messagebody.ThumbFeedback;
import org.cubyte.edumon.client.messaging.messagebody.ThumbRequest;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Jonas
 */
public class ThumbfeedbackController implements Controller {
    private static final int THUMB_SPEED = 5;
    private Main app;
    private boolean countUp;
    private int id;
    private ScheduledFuture<?> counter;

    @FXML
    private ImageView thumb;
    @FXML
    private Label text;

    @Override
    public void setApp(Main app) {
        this.app = app;
    }

    public void resetThumb() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                thumb.setRotate(0);
            }
        });
    }

    public void handleKeyDown() {
        countUp = true;
        counter = app.getScheduledExecutorService().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        double thumbDegree = thumb.getRotate();
                        if (countUp) {
                            thumb.setRotate(++thumbDegree);
                        } else {
                            thumb.setRotate(--thumbDegree);
                        }
                        if (thumbDegree >= 180) {
                            countUp = false;
                        } else if (thumbDegree <= 0) {
                            countUp = true;
                        }
                    }
                });
            }
        }, THUMB_SPEED, THUMB_SPEED, TimeUnit.MILLISECONDS);
    }

    public void handleKeyUp() {
        counter.cancel(true);
        if (app.getNotificationSystem().hideNotification()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    app.getQueue().queue(app.getFactory().create(new ThumbFeedback(id, ((float) (thumb.getRotate()) / 180f - 0.5f) * -1 + 0.5f)));
                }
            });
        }
    }

    public ThumbfeedbackController setId(int id) {
        this.id = id;
        return this;
    }

    public ThumbfeedbackController setFeedbackType(ThumbRequest.FeedbackType type) {
        if (type == ThumbRequest.FeedbackType.thumb) {
            text.setText("Drücken und halten Sie F1 bis der Daumen Ihrem Feedback entspricht. Bei geistiger Abwesenheit F2 drücken.");
        } else {
            text.setText("Drücken und halten Sie F1 bis der Daumen Ihrem Rating entspricht. Bei geistiger Abwesenheit F2 drücken.");
        }
        return this;
    }

    public void handleMentallyAbsent() {
        app.getNotificationSystem().hideNotification();
    }
}
