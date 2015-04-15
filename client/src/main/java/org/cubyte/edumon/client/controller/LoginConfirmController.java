package org.cubyte.edumon.client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import org.cubyte.edumon.client.Main;
import org.cubyte.edumon.client.eventsystem.Victim;
import org.cubyte.edumon.client.messaging.Message;
import org.cubyte.edumon.client.messaging.messagebody.LoginFeedback;
import org.cubyte.edumon.client.messaging.messagebody.WhoAmI;
import org.cubyte.edumon.client.messaging.messagebody.util.Position;

import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.cubyte.edumon.client.Scene.SEAT_CHOOSER;

public class LoginConfirmController implements Controller, Victim<Message> {
    private Main app;
    private int seconds;

    @FXML
    private Label infoBar;
    @FXML
    private Label time;
    @FXML
    private Label green;
    @FXML
    private ProgressIndicator loading;
    @FXML
    private Label problems;
    @FXML
    private Label wait;
    @FXML
    private Button cancel;

    @Override
    public void setApp(Main app) {
        this.app = app;
        app.getQueue().aimAt(LoginFeedback.class, this);
    }

    public void waitForConfirmation() {
        switchWait(true);
        final Position seat = app.getSeat();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                infoBar.setText("Raum " + app.getRoom() + " | " + app.getName() + " | Reihe" + seat.y + " Sitzplatz " + seat.x);
            }
        });

        app.getQueue().queue(app.getFactory().create(new WhoAmI(app.getName(), app.getSeat())));
        app.getQueue().send();

        app.getScheduledExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                app.getQueue().execute();
            }
        });
    }

    private void switchWait(final boolean toWait) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                infoBar.setVisible(toWait);
                time.setVisible(!toWait);
                green.setVisible(!toWait);
                loading.setVisible(toWait);
                wait.setVisible(toWait);
                cancel.setVisible(toWait);
                cancel.setText("Abbrechen");
                problems.setVisible(false);
            }
        });
    }

    public void confirmLogin() {
        if (app.canRunInBackground()) {
            switchWait(false);
            seconds = 2;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    time.setText("Die Applikation wird nun im Hintergrund ausgeführt...");
                }
            });
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

    public void handleCancel() {
        app.changeScene(SEAT_CHOOSER);
    }

    @Override
    public void take(Message bullet) {
        int successCode = ((LoginFeedback) bullet.body).successCode;
        String errors = "";
        if ((successCode & 1) != 0) {
            errors += "Dein Name ist bereits von einer anderen Person angegeben worden.\r\n";
        }
        if ((successCode & 2) != 0) {
            errors += "Der Sitz ist bereits von einer anderen Person belegt.\r\n";
        }
        if ((successCode & 4) != 0) {
            errors += "Der gewählte Name existiert in diesem Kurs nicht.\r\n";
        }
        if ((successCode & 8) != 0) {
            errors += "Der gewählte Sitzplatz existiert in diesem Raum nicht.\r\n";
        }
        if (successCode == 0) {
            confirmLogin();
        } else {
            final String error = errors;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    loading.setVisible(false);
                    wait.setVisible(false);
                    problems.setVisible(true);
                    problems.setText(error);
                    cancel.setText("Zurück");
                }
            });
        }
    }
}
