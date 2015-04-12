package org.cubyte.edumon.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.cubyte.edumon.client.controller.Controller;
import org.cubyte.edumon.client.controller.LoginController;
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
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cubyte.edumon.client.Main.Scene.LOGIN;

public class Main extends Application {
    private static final String TRAY_ICON = "/SystemtrayIcon.png";

    private final KeyListener keyListener;
    private final MouseListener mouseListener;
    private final MicListener micListener;
    private final MessageQueue messageQueue;
    private final MessageFactory messageFactory;
    private TrayIcon trayIcon;
    private final ClientConfig clientConfig;
    private final File appData;
    private Stage stage;
    private final ScheduledExecutorService scheduledExecutorService;

    public Main() {
        keyListener = new KeyListener();
        mouseListener = new MouseListener();
        micListener = new MicListener();
        String separator = File.separator;
        if ("\\".equals(separator)) {
            appData = new File(System.getenv("APPDATA") + separator + "EduMon" + separator + "config");
        } else {
            File folder = new File(System.getProperty("user.home") + separator + ".EduMon");
            if (!folder.exists()) {
              folder.mkdirs();
            }
            appData = new File(System.getProperty("user.home") + separator + ".EduMon" + separator + "config");
        }
        final ObjectMapper mapper = new ObjectMapper();
        clientConfig = new ClientConfig("", "");
        try {
            ClientConfig config = mapper.readValue(appData, ClientConfig.class); // TODO create dir
            clientConfig.server = config.server;
            clientConfig.room = config.room;
        } catch(IOException e) {
            System.err.println("Could not read config.");
            System.err.println(e.getMessage());
        }

        // set config defaults
        if ("".equals(getServer()))
            setServer("http://vps2.code-infection.de/edumon/mailbox.php");
        if ("".equals(getRoom()))
            setRoom("160C");

        messageQueue = new MessageQueue(this);
        messageFactory = new MessageFactory(this, "MODERATOR");
        try {
            trayIcon = new TrayIcon(ImageIO.read(getClass().getResourceAsStream(TRAY_ICON)));
        } catch (IOException e) {
            System.err.println("Could not load tray icon.");
            System.err.println(e.getMessage());
        }

        scheduledExecutorService = Executors.newScheduledThreadPool(5);
    }

    public static void main(String[] args) {
        // Launch JavaFX Application
        Main.launch(args);
    }

    @Override
    public void start(Stage stage) {
        // temporary
        final Main mainMod = new Main();
        mainMod.setServer("http://vps2.code-infection.de/edumon/mailbox.php");
        mainMod.setRoom("170C");
        final MessageQueue messageQueueMod = new MessageQueue(mainMod, true);
        final MessageFactory messageFactoryMod = new MessageFactory(mainMod, "BROADCAST");
        ArrayList<String> list = new ArrayList<>();
        list.add("Jonas Dann");
        list.add("Phillip Schichtel");
        list.add("Marco Dörfler");
        list.add("Niko Berkmann");
        messageQueueMod.queue(messageFactoryMod.create(new NameList(list, "160C", new Dimensions(5, 5))));
        messageQueueMod.send();
        // temporary

        Scene.setApp(this);
        stage.setTitle("Login");
        stage.setResizable(false);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                exit();
            }
        });
        this.stage = stage;
        changeScene(LOGIN);
    }

    public void startBackgroundExecution() {
        addAppToSystemTray();
        registerSensorListeners();
        scheduleExecutors();
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
        scheduledExecutorService.scheduleAtFixedRate(new SensorFetcher(), 0, 1, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new MessageSender(), 0, 1, TimeUnit.SECONDS);
    }

    public enum Scene {
        LOGIN,
        LOADING,
        NAME_CHOOSER,
        SEAT_CHOOSER,
        LOGIN_CONFIRM;

        private static final HashMap<Scene, javafx.scene.Scene> toSceneMap = new HashMap<>();
        private static final HashMap<Scene, Controller> toControllerMap = new HashMap<>();

        static {
            String sceneString;
            String[] split;
            javafx.scene.Scene fxScene;
            for(Scene scene: Scene.values()) {
                sceneString = scene.toString().toLowerCase();
                split = sceneString.split("_");
                sceneString = "";
                for (String string: split) {
                    sceneString += string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
                }

                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(Main.class.getResource("/" + sceneString + ".fxml"));
                    fxScene = new javafx.scene.Scene((Parent) loader.load());

                    toSceneMap.put(scene, fxScene);
                    toControllerMap.put(scene, (Controller) loader.getController());
                } catch (IOException e) {
                    System.err.println("Could not load scene " + sceneString + ".");
                    System.err.println(e.getMessage());
                }
            }
        }

        public static void setApp(Main app) {
            for(Map.Entry<Scene, Controller> entry: toControllerMap.entrySet()) {
                if (entry.getValue() != null) {
                    entry.getValue().setApp(app);
                }
            }
        }

        public javafx.scene.Scene getScene() {
            return toSceneMap.get(this);
        }

        public Controller getController() {
            return toControllerMap.get(this);
        }
    }

    public void changeScene(final Scene scene) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.setScene(scene.getScene());
                stage.show();
            }
        });
    }

    public void hide() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.hide();
            }
        });
    }

    public MessageQueue getQueue() {
        return messageQueue;
    }

    public MessageFactory getFactory() {
        return messageFactory;
    }

    public String getServer() {
        return clientConfig.server;
    }

    public String getRoom() {
        return clientConfig.room;
    }

    public void setServer(String server) {
        clientConfig.server = server;
    }

    public void setRoom(String room) {
        clientConfig.room = room;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
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

    public void exit() {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(appData, clientConfig);
        } catch (IOException e) {
            System.err.println("Could not write configuration.");
            System.err.println(e.getMessage());
        }
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            System.err.println("Could not unregister Native Hook.");
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
        public void actionPerformed(java.awt.event.ActionEvent event) {
            messageQueue.queue(messageFactory.create(new BreakRequest()));
            trayIcon.displayMessage("Hallo", "Du hast eine Pausenanfrage gesendet", TrayIcon.MessageType.INFO); // TODO maybe do it as Popup
        }
    }

    private class ExitListener implements ActionListener {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent event) {
            exit();
        }
    }
}
