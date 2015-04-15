package org.cubyte.edumon.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.cookie.Cookie;
import org.cubyte.edumon.client.messaging.messagebody.NameList;
import org.cubyte.edumon.client.messaging.messagebody.util.Position;

import java.util.Date;

public class RoomState {
    public final String sessionId;
    public String name;
    public Position seat;
    public final NameList nameList;
    public Date timestamp;

    @JsonCreator
    public RoomState(@JsonProperty("sessionId") String sessionId, @JsonProperty("name") String name,
                     @JsonProperty("seat") Position seat, @JsonProperty("nameList") NameList nameList,
                     @JsonProperty("timestamp") Date timestamp) {
        this.sessionId = sessionId;
        this.name = name;
        this.seat = seat;
        this.nameList = nameList;
        this.timestamp = timestamp;
    }

    public RoomState(String sessionId, NameList nameList) {
        this.sessionId = sessionId;
        this.nameList = nameList;
        this.timestamp = new Date();
    }

    @JsonIgnore
    public boolean isOutdated() {
        if (timestamp.getTime() < new Date().getTime() - 60 * 1000) {
            return true;
        }
        return false;
    }
}
