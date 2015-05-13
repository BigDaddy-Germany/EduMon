package org.cubyte.edumon.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cubyte.edumon.client.messaging.messagebody.NameList;
import org.cubyte.edumon.client.messaging.messagebody.util.Position;

import java.util.Date;

/**
 * @author Jonas
 */
public class RoomState {
    public final String sessionId;
    public final NameList nameList;
    public String name;
    public Position seat;
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
        return timestamp.getTime() < new Date().getTime() - 3600 * 1000;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomState roomState = (RoomState) o;

        return !(name != null ? !name.equals(roomState.name) : roomState.name != null) &&
                !(nameList != null ? !nameList.equals(roomState.nameList) : roomState.nameList != null) &&
                !(seat != null ? !seat.equals(roomState.seat) : roomState.seat != null) &&
                !(sessionId != null ? !sessionId.equals(roomState.sessionId) : roomState.sessionId != null) &&
                !(timestamp != null ? !timestamp.equals(roomState.timestamp) : roomState.timestamp != null);
    }

    @Override
    public int hashCode() {
        int result = sessionId != null ? sessionId.hashCode() : 0;
        result = 31 * result + (nameList != null ? nameList.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (seat != null ? seat.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }
}
