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
import org.cubyte.edumon.client.messaging.messagebody.NameList;
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
import java.util.concurrent.ThreadFactory;
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
    private final NotificationSystem notificationSystem;

    private int sentCounter = 0;
    private int receivedCounter = 0;

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

        scheduledExecutorService = Executors.newScheduledThreadPool(5, new ThreadFactory() {
            int id = 0;
            @Override
            public Thread newThread(Runnable r) {
                id++;
                Thread t = new Thread(r, "test" + id);
                t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        System.out.println(t.getName());
                    }
                });
                return t;
            }
        });
        notificationSystem = new NotificationSystem();
    }

    public static void main(String[] args) {
        // Launch JavaFX Application
        Main.launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Prevent JavaFX from closing the jfx thread when all stages are hidden
        Platform.setImplicitExit(false);

        Scene.setApp(this);
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

    public boolean canRunInBackground() {
        String os = System.getProperty("os.name").toLowerCase();
        return SystemTray.isSupported() && (os.contains("win") || os.contains("mac"));
    }

    private void addAppToSystemTray() {
        final OptionsController optionsController = (OptionsController) OPTIONS.getController();
        optionsController.sendKeyData(clientConfig.sendKeyData).sendMouseData(clientConfig.sendMouseData)
                .sendMicData(clientConfig.sendMicData).setDataOverview();
        stage.setTitle("EduMon Client");
        changeScene(OPTIONS);
        if (!canRunInBackground()) {
            optionsController.setOptions(false);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    exit();
                }
            });
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.hide();
                optionsController.setOptions(true);
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent windowEvent) {
                        stage.hide();
                        windowEvent.consume();
                    }
                });
            }
        });

        final PopupMenu popup = new PopupMenu();
        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem breakRequestItem = new MenuItem("Pausenanfrage senden");
        breakRequestItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                messageQueue.queue(messageFactory.create(new BreakRequest()));
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        notificationSystem.showBreakRequestConfirm();
                    }
                });
            }
        });
        MenuItem optionsItem = new MenuItem("Optionen");
        optionsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        stage.show();
                    }
                });
            }
        });
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        resetToLogin();
                    }
                });
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
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                clientConfig.updateRoomStateTimeStamp();
            }
        }, 5, 5, TimeUnit.MINUTES);
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
        return clientConfig.getRoomState().name;
    }

    public Position getSeat() {
        return clientConfig.getRoomState().seat;
    }

    public void setServer(String server) {
        clientConfig.server = server;
        clientConfig.save();
    }

    public void setRoom(String room) {
        clientConfig.room = room;
        clientConfig.save();
    }

    public void setName(String name) {
        if (!name.equals(clientConfig.getRoomState().name)) {
            clientConfig.getRoomState().seat = null;
        }
        clientConfig.getRoomState().name = name;
        clientConfig.save();
    }

    public void setSeat(Position seat) {
        clientConfig.getRoomState().seat = seat;
        clientConfig.save();
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

    public void addRoomState(NameList nameList) {
        clientConfig.addRoomState(messageQueue.getSessionId(), nameList);
    }

    public RoomState getRoomState() {
        return clientConfig.getRoomState();
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

    public void incSent(int n) {
        sentCounter += n;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ((OptionsController) OPTIONS.getController()).setSent(sentCounter);
            }
        });
    }

    public void incReceived(int n) {
        receivedCounter += n;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ((OptionsController) OPTIONS.getController()).setReceived(receivedCounter);
            }
        });
    }

    public NotificationSystem getNotificationSystem() {
        return notificationSystem;
    }
}
