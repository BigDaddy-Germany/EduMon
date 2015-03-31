package org.cubyte.edumon.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cubyte.edumon.client.messaging.MessageFactory;
import org.cubyte.edumon.client.messaging.MessageQueue;
import org.cubyte.edumon.client.messaging.messagebody.BreakRequest;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final String TRAY_ICON = "/SystemtrayIcon.png";

    private final KeyListener keyListener;
    private final MouseListener mouseListener;
    private final MicListener micListener;
    private final MessageQueue messageQueue;
    private final MessageFactory messageFactory;
    private TrayIcon trayIcon;
    private final ClientConfig clientConfig;
    private final File appData;

    public Main() {
        keyListener = new KeyListener();
        mouseListener = new MouseListener();
        micListener = new MicListener();
        String separator = File.separator;
        if ("\\".equals(separator)) {
            appData = new File(System.getenv("APPDATA") + separator + "EduMon" + separator + "config");
        } else {
            appData = new File(System.getProperty("user.home") + separator + ".EduMon" + separator + "config");
        }
        final ObjectMapper mapper = new ObjectMapper();
        clientConfig = new ClientConfig("", "");
        try {
            ClientConfig config = mapper.readValue(appData, ClientConfig.class); // TODO create dir
            clientConfig.server = config.server;
            clientConfig.room = config.room;
        } catch(IOException e) {
            System.err.println(e.getMessage());
        }
        messageQueue = new MessageQueue(this);
        messageFactory = new MessageFactory(this, "MODERATOR");
        try {
            trayIcon = new TrayIcon(ImageIO.read(Main.class.getResourceAsStream(TRAY_ICON)));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        final Main main = new Main();

        main.addAppToSystemTray();

        // temporary
        final Main mainMod = new Main();
        final MessageQueue messageQueueMod = new MessageQueue(mainMod, true);
        final MessageFactory messageFactoryMod = new MessageFactory(mainMod, "BROADCAST");
        ArrayList<String> list = new ArrayList<>();
        list.add("Jonas Dann");
        messageQueueMod.queue(messageFactoryMod.create(new NameList(list, "160C", new Dimensions(5, 5))));
        messageQueueMod.send();

        if ("".equals(main.getServer()))
            main.setServer("http://vps2.code-infection.de/edumon/mailbox.php");
        if ("".equals(main.getRoom()))
            main.setRoom("160C");
        // temporary

        main.messageQueue.send();
        //TODO what is when no namelist is on the server?

        main.messageQueue.queue(main.messageFactory.create(new WhoAmI("Jonas Dann", new Position(1, 1))));
        main.messageQueue.send();

        main.registerSensorListeners();
        main.scheduleExecutors();
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
        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem breakRequestItem = new MenuItem("Pausenanfrage senden");
        breakRequestItem.addActionListener(new BreakRequestListener());
        MenuItem optionsItem = new MenuItem("Einstellungen"); // TODO add listener
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

    private void scheduleExecutors() {
        final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new SensorFetcher(), 0, 1, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new MessageSender(), 0, 1, TimeUnit.SECONDS);
    }

    public MessageQueue getQueue() {
        return messageQueue;
    }

    public String getServer() {
        return clientConfig.server;
    }

    public String getRoom() {
        return clientConfig.room;
    }

    private void setServer(String server) {
        clientConfig.server = server;
    }

    private void setRoom(String room) {
        clientConfig.room = room;
    }

    private class SensorFetcher implements Runnable {
        private int keystrokes;
        private int mouseclicks;
        private double mousedistance;
        private double micLevel;

        @Override
        public void run() {
            keystrokes = keyListener.fetchStrokes();
            mouseclicks = mouseListener.fetchClicks();
            mousedistance = mouseListener.fetchDistance();
            micLevel = micListener.fetchLevel();

            messageQueue.queue(messageFactory.create(new SensorData(keystrokes, mousedistance, mouseclicks, micLevel)));
        }
    }

    private void exit() {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(appData, clientConfig);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            System.err.println(e.getMessage());
        }
        System.runFinalization();
        System.exit(0);
    }

    private class MessageSender implements Runnable {
        @Override
        public void run() {
            messageQueue.send();
        }
    }

    private class BreakRequestListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            messageQueue.queue(messageFactory.create(new BreakRequest()));
            trayIcon.displayMessage("Hallo", "Du hast eine Pausenanfrage gesendet", TrayIcon.MessageType.INFO); // TODO maybe do it as Popup
        }
    }

    private class ExitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            exit();
        }
    }
}
