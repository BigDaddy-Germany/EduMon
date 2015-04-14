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
        if (app.canRunInBackground()) {
            seconds = 2;
            final ScheduledFuture<?> scheduledFuture = app.getScheduledExecutorService().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            String text = "Die Applikation wird nun im Hintergrund ausgeführt";
                            for (int i = 0; i < seconds + 1; i++) {
                                text += ".";
                            }
                            time.setText(text);
                        }
                    });
                    if (seconds > 0) {
                        seconds--;
                    }
                }
            }, 1, 1, SECONDS);
            app.getScheduledExecutorService().schedule(new Runnable() {
                @Override
                public void run() {
                    scheduledFuture.cancel(true);
                    app.startBackgroundExecution();
                }
            }, seconds + 1, SECONDS);
        } else {
            app.startBackgroundExecution();
        }
    }
}
