package org.cubyte.edumon.client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.cubyte.edumon.client.Main;

import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

public class LoginConfirmController implements Controller {
    private Main app;
    private int seconds;

    @FXML
    private Label time;

    @Override
    public void setApp(Main app) {
        this.app = app;
    }

    public void confirmLogin() {
        seconds = 5;
        final ScheduledFuture<?> scheduledFuture = app.getScheduledExecutorService().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        time.setText("Diese Meldung verschwindet in " + seconds + " Sekunden");
                    }
                });
                if (seconds > 0) {
                    seconds--;
                }
            }
        }, 0, 1, SECONDS);
        app.getScheduledExecutorService().schedule(new Runnable() {
            @Override
            public void run() {
                scheduledFuture.cancel(true);
                app.hide();
            }
        }, seconds, SECONDS);
    }
}
