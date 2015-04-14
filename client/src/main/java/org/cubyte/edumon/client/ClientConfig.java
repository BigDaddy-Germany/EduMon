package org.cubyte.edumon.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Pos;
import org.apache.http.cookie.Cookie;
import org.cubyte.edumon.client.messaging.messagebody.NameList;
import org.cubyte.edumon.client.messaging.messagebody.util.Position;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ClientConfig {
    public String server;
    public String room;
    public String name;
    public Position seat;
    public boolean sendKeyData;
    public boolean sendMouseData;
    public boolean sendMicData;
    public final Map<String, RoomState> roomStateMap;

    @JsonIgnore
    private static final File file;
    @JsonIgnore
    private static final ObjectMapper mapper;

    static {
        String separator = File.separator;
        if ("\\".equals(separator)) {
            File folder = new File(System.getenv("APPDATA") + separator + "EduMon");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            file = new File(System.getenv("APPDATA") + separator + "EduMon" + separator + "config");
        } else {
            File folder = new File(System.getProperty("user.home") + separator + ".EduMon");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            file = new File(System.getProperty("user.home") + separator + ".EduMon" + separator + "config");
        }
        mapper = new ObjectMapper();
    }

    @JsonCreator
    public ClientConfig(@JsonProperty("server") String server, @JsonProperty("room") String room,
                        @JsonProperty("name") String name, @JsonProperty("seat") Position seat,
                        @JsonProperty("sendKeyData") boolean sendKeyData,
                        @JsonProperty("sendMouseData") boolean sendMouseData,
                        @JsonProperty("sendMicData") boolean sendMicData,
                        @JsonProperty("roomStateMap") Map<String, RoomState> roomStateMap) {
        this.server = server;
        this.room = room;
        this.name = name;
        this.seat = seat;
        this.sendKeyData = sendKeyData;
        this.sendMouseData = sendMouseData;
        this.sendMicData = sendMicData;
        this.roomStateMap = roomStateMap;
        cleanRoomStateMap();
    }

    public static ClientConfig getConfig() {
        ClientConfig config;
        try {
            config = mapper.readValue(file, ClientConfig.class);
        } catch(IOException e) {
            System.err.println("Could not read config.");
            System.err.println(e.getMessage());
            config = new ClientConfig("http://vps2.code-infection.de/edumon", null, "", null, true, true, true, new HashMap<String, RoomState>());
        }
        return config;
    }

    public void save() {
        cleanRoomStateMap();
        try {
            mapper.writeValue(file, this);
        } catch (IOException e) {
            System.err.println("Could not write configuration.");
            System.err.println(e.getMessage());
        }
    }

    public void addRoomState(String sessionId, NameList nameList) {
        roomStateMap.put(room, new RoomState(sessionId, nameList));
    }

    public void updateRoomStateTimeStamp() {
        roomStateMap.get(room).timestamp = new Date();
    }

    @JsonIgnore
    public RoomState getRoomState() {
        RoomState state = roomStateMap.get(room);
        if (state != null && !state.isOutdated()) {
            return state;
        }
        return null;
    }

    public void cleanRoomStateMap() {
        for(Map.Entry<String, RoomState> entry: roomStateMap.entrySet()) {
            if (entry.getValue().isOutdated()) {
                roomStateMap.remove(entry.getKey());
            }
        }
    }
}
