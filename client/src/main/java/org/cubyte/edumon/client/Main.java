package org.cubyte.edumon.client;

import org.cubyte.edumon.client.messaging.MessageFactory;
import org.cubyte.edumon.client.messaging.MessageQueue;
import org.cubyte.edumon.client.messaging.messagebodies.Sensordata;
import org.cubyte.edumon.client.sensorlistener.KeyListener;
import org.cubyte.edumon.client.sensorlistener.MicListener;
import org.cubyte.edumon.client.sensorlistener.MouseListener;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(e.getMessage());

            System.exit(1);
        }
        final KeyListener keyListener = new KeyListener();
        final MouseListener mouseListener = new MouseListener();
        GlobalScreen.addNativeKeyListener(keyListener);
        GlobalScreen.addNativeMouseListener(mouseListener);
        GlobalScreen.addNativeMouseMotionListener(mouseListener);

        final MicListener micListener = new MicListener();

        final MessageQueue messageQueue = new MessageQueue("");
        final MessageFactory messageFactory = new MessageFactory(0, "jonas", "mod", "160C");

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

                messageQueue.queue(messageFactory.create(new Sensordata(keystrokes, mousedistance, mouseclicks, micLevel)));
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
