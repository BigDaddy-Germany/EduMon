package org.cubyte.edumon.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
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
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cubyte.edumon.client.Scene.*;

public class Main extends Application {
    private static final String ICON = "/EduMon.png";
    private static final String SVG = "/EduMon.svg";
    private static final String SVG_LOCATION;
    private static final String DESKTOP = "/edumon.desktop";
    private static final String DESKTOP_LOCATION;

    static {
        String userHome = System.getProperty("user.home");
        SVG_LOCATION = userHome + "/.local/share/pixmaps";
        DESKTOP_LOCATION = userHome + "/.local/share/applications";
    }

    private final KeyListener keyListener;
    private final MouseListener mouseListener;
    private final MicListener micListener;
    private final MessageQueue messageQueue;
    private final MessageFactory messageFactory;
    private final Image appIcon;
    private final ClientConfig clientConfig;
    private final ScheduledExecutorService scheduledExecutorService;
    private final NotificationSystem notificationSystem;
    private TrayIcon trayIcon;
    private Stage stage;
    private ScheduledFuture<?> sensorFetcherFuture;
    private ScheduledFuture<?> messageSenderFuture;
    private ScheduledFuture<?> eventExecutorFuture;
    private ScheduledFuture<?> timeStampUpdaterFuture;

    private int sentCounter = 0;
    private int receivedCounter = 0;

    public Main() {
        keyListener = new KeyListener();
        mouseListener = new MouseListener();
        micListener = new MicListener();

        clientConfig = ClientConfig.getConfig();

        messageQueue = new MessageQueue(this);
        messageFactory = new MessageFactory(this, "MODERATOR");

        if (isLinux()) {
            File desktop = new File(DESKTOP_LOCATION + DESKTOP);
            if (!desktop.exists()) {
                File folder = new File(DESKTOP_LOCATION);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                try {
                    Files.copy(getClass().getResourceAsStream(DESKTOP), Paths.get(DESKTOP_LOCATION + DESKTOP));
                } catch (IOException e) {
                    System.err.println("Could not copy desktop.");
                    System.err.println(e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
            File svg = new File(SVG_LOCATION + SVG);
            if (!svg.exists()) {
                File folder = new File(SVG_LOCATION);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                try {
                    Files.copy(getClass().getResourceAsStream(SVG), Paths.get(SVG_LOCATION + SVG));
                } catch (IOException e) {
                    System.err.println("Could not copy svg.");
                    System.err.println(e.getMessage());
                }
            }
            try {
                FileReader fileReader = new FileReader(desktop);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                List<String> lines = new ArrayList<>();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.startsWith("Exec=") && !line.startsWith("Icon=")) {
                        lines.add(line);
                    }
                }
                bufferedReader.close();
                fileReader.close();
                lines.add("Exec=java -cp " + System.getProperty("java.class.path") + " " + Main.class.getName());
                lines.add("Icon=" + SVG_LOCATION + SVG);

                FileWriter fileWriter = new FileWriter(desktop);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                for (String fuck : lines) {
                    bufferedWriter.write(fuck + "\n");
                }
                bufferedWriter.flush();
                bufferedWriter.close();
                fileWriter.close();
            } catch (FileNotFoundException e) {
                System.err.println("Could not find desktop file.");
                System.err.println(e.getMessage());
            } catch (IOException e) {
                System.err.println("Could not read or write desktop file.");
                System.err.println(e.getMessage());
            }
        }

        appIcon = new Image(ICON);
        try {
            trayIcon = new TrayIcon(ImageIO.read(getClass().getResourceAsStream(ICON)));

            final PopupMenu popup = new PopupMenu();

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
                            changeScene(EMPTY);
                            changeScene(OPTIONS);
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
                            SystemTray.getSystemTray().remove(trayIcon);
                            unregisterSensorListeners();
                            killExecutors();
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
        notificationSystem = new NotificationSystem(this);
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
        this.stage = stage;
        stage.setResizable(false);
        stage.getIcons().add(appIcon);
        resetToLogin();
    }

    public void resetToLogin() {
        stage.setTitle("Login");
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                exit();
            }
        });
        changeScene(EMPTY);
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
        //drain sensors
        keyListener.fetchStrokes();
        mouseListener.fetchClicks();
        mouseListener.fetchDistance();
        micListener.fetchLevel();
    }

    public void unregisterSensorListeners() {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            System.err.println("Could not unregister Native Hook.");
            System.err.println(e.getMessage());
        }
    }

    public boolean isLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return !(os.contains("win") || os.contains("mac"));
    }

    public boolean canRunInBackground() {
        return SystemTray.isSupported() && !isLinux();
    }

    private void addAppToSystemTray() {
        final OptionsController optionsController = (OptionsController) OPTIONS.getController();
        optionsController.sendKeyData(clientConfig.sendKeyData).sendMouseData(clientConfig.sendMouseData)
                .sendMicData(clientConfig.sendMicData).setDataOverview();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!canRunInBackground() || trayIcon == null) {
                    stage.setTitle("EduMon Client");
                    changeScene(OPTIONS);
                    optionsController.setOptions(false);
                    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent windowEvent) {
                            exit();
                        }
                    });
                    return;
                }

                stage.setTitle("Options");
                optionsController.setOptions(true);
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent windowEvent) {
                        stage.hide();
                        windowEvent.consume();
                    }
                });
                stage.hide();
            }
        });

        if (canRunInBackground() && trayIcon != null) {
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException e) {
                System.err.println("System tray icon could not be added.");
                System.err.println(e.getMessage());
            }
        }
    }

    private void scheduleExecutors() {
        messageQueue.clear();
        sensorFetcherFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
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
        messageSenderFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                messageQueue.send();
            }
        }, 0, 1, TimeUnit.SECONDS);
        eventExecutorFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                messageQueue.execute();
            }
        }, 1, 1, TimeUnit.SECONDS);
        timeStampUpdaterFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                clientConfig.updateRoomStateTimeStamp();
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    public void killExecutors() {
        sensorFetcherFuture.cancel(true);
        messageSenderFuture.cancel(true);
        eventExecutorFuture.cancel(true);
        timeStampUpdaterFuture.cancel(true);
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

    public void setServer(String server) {
        clientConfig.server = server;
        clientConfig.save();
    }

    public String getRoom() {
        return clientConfig.room;
    }

    public void setRoom(String room) {
        clientConfig.room = room;
        clientConfig.save();
    }

    public String getName() {
        RoomState roomState = clientConfig.getRoomState();
        if (roomState != null && !"".equals(roomState.name)) {
            return clientConfig.getRoomState().name;
        }
        return clientConfig.name;
    }

    public void setName(String name) {
        RoomState roomState = clientConfig.getRoomState();
        if (roomState != null) {
            if (!name.equals(roomState.name)) {
                roomState.seat = null;
            }
            roomState.name = name;
        }
        clientConfig.name = name;
        clientConfig.save();
    }

    public Position getSeat() {
        RoomState roomState = clientConfig.getRoomState();
        if (roomState != null && roomState.seat != null) {
            return clientConfig.getRoomState().seat;
        }
        return clientConfig.seat;
    }

    public void setSeat(Position seat) {
        RoomState roomState = clientConfig.getRoomState();
        if (roomState != null) {
            clientConfig.getRoomState().seat = seat;
        }
        clientConfig.seat = seat;
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

    public Image getAppIcon() {
        return appIcon;
    }

    public void exit() {
        clientConfig.save();

        unregisterSensorListeners();
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
