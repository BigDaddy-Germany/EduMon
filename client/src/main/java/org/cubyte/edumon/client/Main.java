package org.cubyte.edumon.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.cubyte.edumon.client.controller.OptionsController;
import org.cubyte.edumon.client.messaging.MessageFactory;
import org.cubyte.edumon.client.messaging.MessageQueue;
import org.cubyte.edumon.client.messaging.messagebody.BreakRequest;
import org.cubyte.edumon.client.messaging.messagebody.SensorData;
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
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cubyte.edumon.client.Scene.LOGIN;
import static org.cubyte.edumon.client.Scene.OPTIONS;

public class Main extends Application {
    private static final String TRAY_ICON = "/SystemtrayIcon.png";

    private final KeyListener keyListener;
    private final MouseListener mouseListener;
    private final MicListener micListener;
    private final MessageQueue messageQueue;
    private final MessageFactory messageFactory;
    private TrayIcon trayIcon;
    private final ClientConfig clientConfig;
    private Stage stage;
    private final ScheduledExecutorService scheduledExecutorService;

    public Main() {
        keyListener = new KeyListener();
        mouseListener = new MouseListener();
        micListener = new MicListener();

        clientConfig = ClientConfig.getConfig();

        messageQueue = new MessageQueue(this);
        messageFactory = new MessageFactory(this, "MODERATOR");

        try { //TODO window iconse
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
        /*// temporary
        final Main mainMod = new Main();
        mainMod.setServer("http://vps2.code-infection.de/edumon");
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
        */// temporary

        org.cubyte.edumon.client.Scene.setApp(this);
        stage.setResizable(false);
        this.stage = stage;
        resetToLogin();
    }

    public void resetToLogin() {
        stage.setTitle("Login"); //TODO maybe rename title in login screens
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                exit();
            }
        });
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
        String os = System.getProperty("os.name").toLowerCase();
        final OptionsController optionsController = (OptionsController) OPTIONS.getController();
        optionsController.sendKeyData(clientConfig.sendKeyData).sendMouseData(clientConfig.sendMouseData)
                .sendMicData(clientConfig.sendMicData).setDataOverview();
        stage.setTitle("Optionen");
        changeScene(OPTIONS);
        if (!SystemTray.isSupported() || (!os.contains("win") && !os.contains("mac"))) {
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    optionsController.showPopup();
                    windowEvent.consume();
                }
            });
            return;
        }
        stage.hide();
        optionsController.hideSendBreakrequest();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                stage.hide();
                windowEvent.consume();
            }
        });

        final PopupMenu popup = new PopupMenu();
        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem breakRequestItem = new MenuItem("Pausenanfrage senden");
        breakRequestItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                messageQueue.queue(messageFactory.create(new BreakRequest()));
                trayIcon.displayMessage("Hallo", "Du hast eine Pausenanfrage gesendet", TrayIcon.MessageType.INFO); // TODO maybe do it as Popup
            }
        });
        MenuItem optionsItem = new MenuItem("Optionen");
        optionsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stage.show();
            }
        });
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetToLogin();
            }
        });
        MenuItem exitItem = new MenuItem("Anwendung schließen");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });

        popup.add(breakRequestItem);
        popup.addSeparator();
        popup.add(optionsItem);
        popup.add(logoutItem);
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
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            private int keystrokes;
            private int mouseclicks;
            private double mousedistance;
            private double micLevel;

            @Override
            public void run() {
                if (clientConfig.sendKeyData) {
                    keystrokes = keyListener.fetchStrokes();
                } else {
                    keystrokes = 0;
                }
                if (clientConfig.sendMouseData) {
                    mouseclicks = mouseListener.fetchClicks();
                    mousedistance = mouseListener.fetchDistance();
                } else {
                    mouseclicks = 0;
                    mousedistance = 0;
                }
                if (clientConfig.sendMicData) {
                    micLevel = micListener.fetchLevel();
                } else {
                    micLevel = 0;
                }

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

    public void changeScene(final org.cubyte.edumon.client.Scene scene) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.setScene(scene.getScene());
                stage.show();
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

    public String getName() {
        return clientConfig.name;
    }

    public Position getSeat() {
        return clientConfig.seat;
    }

    public void setServer(String server) {
        clientConfig.server = server;
    }

    public void setRoom(String room) {
        clientConfig.room = room;
    }

    public void setName(String name) {
        clientConfig.name = name;
    }

    public void setSeat(Position seat) {
        clientConfig.seat = seat;
    }

    public void setSendKeyData(boolean sendKeyData) {
        clientConfig.sendKeyData = sendKeyData;
        clientConfig.save();
    }

    public void setSendMouseData(boolean sendMouseData) {
        clientConfig.sendMouseData = sendMouseData;
        clientConfig.save();
    }

    public void setSendMicData(boolean sendMicData) {
        clientConfig.sendMicData = sendMicData;
        clientConfig.save();
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public void exit() {
        clientConfig.save();

        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            System.err.println("Could not unregister Native Hook.");
            System.err.println(e.getMessage());
        }
        System.runFinalization();
        System.exit(0);
    }
}
