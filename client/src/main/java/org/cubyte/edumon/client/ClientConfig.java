package org.cubyte.edumon.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientConfig {
    public String server;
    public String room;

    @JsonCreator
    public ClientConfig(@JsonProperty("server") String server, @JsonProperty("room") String room) {
        this.server = server;
        this.room = room;
    }
}
