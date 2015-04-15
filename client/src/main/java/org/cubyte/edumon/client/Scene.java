package org.cubyte.edumon.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.cubyte.edumon.client.controller.Controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public enum Scene {
    LOGIN,
    LOADING,
    NAME_CHOOSER,
    SEAT_CHOOSER,
    LOGIN_CONFIRM,
    OPTIONS,
    BREAK_REQUEST_CONFIRM,
    THUMBFEEDBACK,
    EMPTY;
    //REVIEW;

    private static final HashMap<Scene, javafx.scene.Scene> toSceneMap = new HashMap<>();
    private static final HashMap<Scene, Controller> toControllerMap = new HashMap<>();

    static {
        String sceneString;
        String[] split;
        javafx.scene.Scene fxScene;
        for (Scene scene : Scene.values()) {
            sceneString = scene.toString().toLowerCase();
            split = sceneString.split("_");
            sceneString = "";
            for (String string : split) {
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
        for (Map.Entry<Scene, Controller> entry : toControllerMap.entrySet()) {
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
