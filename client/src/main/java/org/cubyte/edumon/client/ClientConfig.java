package org.cubyte.edumon.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                        @JsonProperty("roomNameListMap") Map<String, RoomState> roomStateMap) {
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
            config = new ClientConfig("http://vps2.code-infection.de/edumon", "", "", null, true, true, true, new HashMap<String, RoomState>());
        }
        return config;
    }

    public void save() {
        try {
            mapper.writeValue(file, this);
        } catch (IOException e) {
            System.err.println("Could not write configuration.");
            System.err.println(e.getMessage());
        }
    }

    /*public void addRoomState(String room, NameList nameList) {
        roomNameListMap.put(room, new Tuple<>(nameList, new Date()));
    }

    public void updateRoomStateTimeStamp(String room) {
        Tuple<NameList, Date> old = roomNameListMap.get(room);
        roomNameListMap.put(room, new Tuple<>(old.getT1(), new Date()));
    }

    public NameList getRoomState(String room) {
        Tuple<NameList, Date> nameList = roomNameListMap.get(room);
        if (nameList.getT2().getTime() > new Date().getTime() - 3600 * 1000) {
            return nameList.getT1();
        }
        return null;
    }*/ // TODO

    public void cleanRoomStateMap() {

    }

    public class RoomState {
        public String sessionId;
        public String server;
        public String name;
        public Position seat;
        public NameList nameList;
        public Date timestamp;
    }
}
