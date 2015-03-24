package org.cubyte.edumon.client;

import org.cubyte.edumon.client.messaging.Message;
import org.cubyte.edumon.client.messaging.MessageFactory;
import org.cubyte.edumon.client.messaging.MessageQueue;
import org.cubyte.edumon.client.messaging.messagebody.NameList;
import org.cubyte.edumon.client.messaging.messagebody.SensorData;
import org.cubyte.edumon.client.messaging.messagebody.WhoAmI;
import org.cubyte.edumon.client.messaging.messagebody.util.Dimensions;
import org.cubyte.edumon.client.messaging.messagebody.util.Position;
import org.cubyte.edumon.client.sensorlistener.KeyListener;
import org.cubyte.edumon.client.sensorlistener.MicListener;
import org.cubyte.edumon.client.sensorlistener.MouseListener;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        final String ROOM = "160C";

        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        /*try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(e.getMessage());

            System.exit(1);
        }*/
        final KeyListener keyListener = new KeyListener();
        final MouseListener mouseListener = new MouseListener();
        GlobalScreen.addNativeKeyListener(keyListener);
        GlobalScreen.addNativeMouseListener(mouseListener);
        GlobalScreen.addNativeMouseMotionListener(mouseListener);

        final MicListener micListener = new MicListener();

        final MessageQueue messageQueue = new MessageQueue("http://vps2.code-infection.de/edumon/mailbox.php", ROOM);

        // temporary
        final MessageQueue messageQueueMod = new MessageQueue("http://vps2.code-infection.de/edumon/mailbox.php", ROOM, true);
        final MessageFactory messageFactoryMod = new MessageFactory(0, "MODERATOR", "BROADCAST", ROOM);
        List<String> list = new ArrayList<>();
        list.add("Jonas Dann");
        messageQueueMod.queue(messageFactoryMod.create(new NameList(list, ROOM, new Dimensions(5, 5))));
        messageQueueMod.send();
        // temporary

        messageQueue.send();
        Message nameList = messageQueue.unload(); //TODO what is when no namelist is on the server?

        final MessageFactory messageFactory = new MessageFactory(0, "Jonas Dann", "MODERATOR", ROOM);

        messageQueue.queue(messageFactory.create(new WhoAmI("Jonas Dann", new Position(1, 1))));
        messageQueue.send();

        final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            int keystrokes;
            int mouseclicks;
            int mousedistance;
            double micLevel;
            @Override
            public void run() {
                keystrokes = keyListener.fetchStrokes();
                mouseclicks = mouseListener.fetchClicks();
                mousedistance = mouseListener.fetchDistance();
                micLevel = micListener.fetchLevel();

                messageQueue.queue(messageFactory.create(new SensorData(keystrokes, mousedistance, mouseclicks, micLevel)));
            }
        }, 0, 1, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                messageQueue.send();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}
