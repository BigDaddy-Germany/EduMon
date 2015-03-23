package org.cubyte.edumon.client.messaging.messagebodies;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cubyte.edumon.client.messaging.messagebodies.util.Position;

public class WhoAmI implements MessageBody {
    public final String name;
    public final Position seat;

    @JsonCreator
    public WhoAmI(@JsonProperty("name") String name, @JsonProperty("seat") Position seat) {
        this.name = name;
        this.seat = seat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WhoAmI whoAmI = (WhoAmI) o;

        return name.equals(whoAmI.name) && seat.equals(whoAmI.seat);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + seat.hashCode();
        return result;
    }
}
