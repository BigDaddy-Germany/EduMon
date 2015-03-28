package org.cubyte.edumon.client;

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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private String Room = "160C"; // TODO Set room in login
    private final KeyListener keyListener;
    private final MouseListener mouseListener;
    private final MicListener micListener;
    private final MessageQueue messageQueue;
    private final MessageFactory messageFactory;

    public Main() {
        keyListener = new KeyListener();
        mouseListener = new MouseListener();
        micListener = new MicListener();
        messageQueue = new MessageQueue("http://vps2.code-infection.de/edumon/mailbox.php", Room); // TODO load server and room from config
        messageFactory = new MessageFactory(messageQueue, "MODERATOR", Room);
    }

    public static void main(String[] args) {
        final Main main = new Main();

        main.addAppToSystemTray();

        // temporary
        final MessageQueue messageQueueMod = new MessageQueue("http://vps2.code-infection.de/edumon/mailbox.php", main.Room, true);
        final MessageFactory messageFactoryMod = new MessageFactory(messageQueueMod, "BROADCAST", main.Room);
        ArrayList<String> list = new ArrayList<>();
        list.add("Jonas Dann");
        messageQueueMod.queue(messageFactoryMod.create(new NameList(list, main.Room, new Dimensions(5, 5))));
        messageQueueMod.send();
        // temporary

        main.messageQueue.send();
        //TODO what is when no namelist is on the server?

        main.messageQueue.queue(main.messageFactory.create(new WhoAmI("Jonas Dann", new Position(1, 1))));
        main.messageQueue.send();

        main.registerSensorListeners();

        final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new SensorFetcher(main), 0, 1, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new MessageSender(main), 0, 1, TimeUnit.SECONDS);
    }

    private void registerSensorListeners() {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(e.getMessage());

            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(keyListener);
        GlobalScreen.addNativeMouseListener(mouseListener);
        GlobalScreen.addNativeMouseMotionListener(mouseListener);
        micListener.fetchLevel();
    }

    private void addAppToSystemTray() {
        if (!SystemTray.isSupported()) {
            // TODO Show window with message "System tray not supported" and "Breakrequest", "Options" and "Exit" Buttons
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon;
        try {
            trayIcon = new TrayIcon(ImageIO.read(new File("./client/src/main/resources/SystemtrayIcon.png")));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return;
        }
        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem breakRequestItem = new MenuItem("Pausenanfrage senden");
        MenuItem optionsItem = new MenuItem("Einstellungen");
        MenuItem exitItem = new MenuItem("Anwendung schließen");
        exitItem.addActionListener(new ExitListener());

        popup.add(breakRequestItem);
        popup.addSeparator();
        popup.add(optionsItem);
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("System tray icon could not be added.");
            System.err.println(e.getMessage());
        }
    }

    private static class SensorFetcher implements Runnable {
        private final Main owner;

        private int keystrokes;
        private int mouseclicks;
        private int mousedistance;
        private double micLevel;

        public SensorFetcher(Main owner) {
            this.owner = owner;
        }

        @Override
        public void run() {
            keystrokes = owner.keyListener.fetchStrokes();
            mouseclicks = owner.mouseListener.fetchClicks();
            mousedistance = owner.mouseListener.fetchDistance();
            micLevel = owner.micListener.fetchLevel();

            owner.messageQueue.queue(owner.messageFactory.create(new SensorData(keystrokes, mousedistance, mouseclicks, micLevel)));
        }
    }

    private static class MessageSender implements Runnable {
        private final Main owner;

        public MessageSender(Main owner) {
            this.owner = owner;
        }

        @Override
        public void run() {
            owner.messageQueue.send();
        }
    }

    private class ExitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException e) {
                System.err.println(e.getMessage());
            }
            System.runFinalization();
            System.exit(0);
        }
    }
}
