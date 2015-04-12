package org.cubyte.edumon.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cubyte.edumon.client.messaging.messagebody.util.Position;

import java.io.File;
import java.io.IOException;

public class ClientConfig {
    public String server;
    public String room;
    public String name;
    public Position seat;
    public boolean sendKeyData;
    public boolean sendMouseData;
    public boolean sendMicData;

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
                        @JsonProperty("sendMicData") boolean sendMicData) {
        this.server = server;
        this.room = room;
        this.name = name;
        this.seat = seat;
        this.sendKeyData = sendKeyData;
        this.sendMouseData = sendMouseData;
        this.sendMicData = sendMicData;
    }

    public static ClientConfig getConfig() {
        ClientConfig config;
        try {
            config = mapper.readValue(file, ClientConfig.class);
        } catch(IOException e) {
            System.err.println("Could not read config.");
            System.err.println(e.getMessage());
            config = new ClientConfig("http://vps2.code-infection.de/edumon", "", "", null, true, true, true);
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
}
