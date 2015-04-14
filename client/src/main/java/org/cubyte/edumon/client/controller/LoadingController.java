package org.cubyte.edumon.client.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.cubyte.edumon.client.Main;
import org.cubyte.edumon.client.RoomState;
import org.cubyte.edumon.client.eventsystem.Victim;
import org.cubyte.edumon.client.messaging.Message;
import org.cubyte.edumon.client.messaging.MessageQueue;
import org.cubyte.edumon.client.messaging.messagebody.NameList;

import java.util.concurrent.*;

import static javafx.scene.input.KeyCode.ESCAPE;
import static org.cubyte.edumon.client.Scene.LOGIN;
import static org.cubyte.edumon.client.Scene.NAME_CHOOSER;

public class LoadingController implements Victim<Message>, Controller {
    private Main app;
    private ScheduledFuture<?> send;
    private Future<?> execute;
    @FXML
    private Pane pane;
    @FXML
    private Label roomAndServer;

    @FXML
    private void initialize() {
        pane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == ESCAPE) {
                    handleCancel();
                }
            }
        });
    }

    @Override
    public void setApp(Main app) {
        this.app = app;
        this.app.getQueue().aimAt(NameList.class, this);
    }

    public void setInfoBar() {
        this.roomAndServer.setText("Raum " + app.getRoom() + " | Server: " + app.getServer());
    }

    public void getNameList() {
        RoomState state = app.getRoomState();
        if(state == null) {
            final MessageQueue queue = app.getQueue();
            if (!queue.ping()) {
                app.changeScene(LOGIN);
                ((LoginController) LOGIN.getController()).serverNotReachable();
                return;
            }
            send = app.getScheduledExecutorService().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    queue.send();
                }
            }, 0, 5, TimeUnit.SECONDS);
            execute = app.getScheduledExecutorService().submit(new Runnable() {
                @Override
                public void run() {
                    queue.execute();
                    send.cancel(true);
                }
            });
        } else {
            app.getQueue().setSessionId();
            toNameChooser();
        }
    }

    @FXML
    private void handleCancel() {
        app.changeScene(LOGIN);
        send.cancel(true);
        execute.cancel(true);
    }

    @Override
    public void take(Message bullet) {
        NameList nameList = ((NameList) bullet.body);
        app.addRoomState(nameList);
        toNameChooser();
    }

    private void toNameChooser() {
        app.changeScene(NAME_CHOOSER);
        ((NameChooserController)NAME_CHOOSER.getController()).setNameList();
    }
}
