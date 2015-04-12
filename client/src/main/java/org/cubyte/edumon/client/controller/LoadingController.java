package org.cubyte.edumon.client.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.cubyte.edumon.client.Main;
import org.cubyte.edumon.client.eventsystem.Victim;
import org.cubyte.edumon.client.messaging.Message;
import org.cubyte.edumon.client.messaging.MessageQueue;
import org.cubyte.edumon.client.messaging.messagebody.NameList;

import java.util.Queue;
import java.util.concurrent.*;

import static javafx.scene.input.KeyCode.ESCAPE;
import static org.cubyte.edumon.client.Main.Scene.LOGIN;
import static org.cubyte.edumon.client.Main.Scene.NAME_CHOOSER;
import static org.cubyte.edumon.client.Main.Scene.SEAT_CHOOSER;

public class LoadingController implements Victim<Message>, Controller {
    private Main app;
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
    }

    public void setRoomAndServer(String room, String server) {
        this.roomAndServer.setText("Raum " + room + " | Server: " + server);
    }

    public void getNameList() {
        final MessageQueue queue = app.getQueue();
        if(!queue.ping()) {
            app.changeScene(LOGIN);
            ((LoginController)LOGIN.getController()).serverNotReachable();
            return;
        }
        queue.aimAt(NameList.class, this);
        final ScheduledFuture<?> scheduledFuture = app.getScheduledExecutorService().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                queue.send();
            }
        }, 0, 5, TimeUnit.SECONDS);
        app.getScheduledExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                queue.execute();
                scheduledFuture.cancel(true);
            }
        });
    }

    @FXML
    private void handleCancel() {
        app.changeScene(LOGIN);
    }

    @Override
    public void take(Message bullet) {
        NameList nameList = ((NameList) bullet.body);
        //TODO save nameList somewhere in config
        ((NameChooserController)NAME_CHOOSER.getController()).setNameList(nameList.names);
        ((SeatChooserController)SEAT_CHOOSER.getController()).storeDimensions(nameList.dimensions);
        app.changeScene(NAME_CHOOSER);
    }
}
