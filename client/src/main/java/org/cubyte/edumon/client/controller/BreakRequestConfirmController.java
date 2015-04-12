package org.cubyte.edumon.client.controller;

import javafx.application.Platform;
import org.cubyte.edumon.client.Main;

import java.util.concurrent.TimeUnit;

public class BreakRequestConfirmController implements Controller {
    private Main app;

    @Override
    public void setApp(Main app) {
        this.app = app;
    }

    public void show() {
        app.getScheduledExecutorService().schedule(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        app.getNotificationSystem().hide();
                    }
                });
            }
        }, 3, TimeUnit.SECONDS);
    }
}
