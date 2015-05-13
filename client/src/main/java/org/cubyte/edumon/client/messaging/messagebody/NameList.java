package org.cubyte.edumon.client.messaging.messagebody;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cubyte.edumon.client.messaging.messagebody.util.Dimensions;

import java.util.List;

/**
 * @author Jonas
 */
public class NameList implements MessageBody {
    public final List<String> names;
    public final String room;
    public final Dimensions dimensions;

    @JsonCreator
    public NameList(@JsonProperty("names") List<String> names, @JsonProperty("room") String room, @JsonProperty("dimensions") Dimensions dimensions) {
        this.names = names;
        this.room = room;
        this.dimensions = dimensions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NameList nameList = (NameList) o;

        return dimensions.equals(nameList.dimensions) && names.equals(nameList.names) && room.equals(nameList.room);
    }

    @Override
    public int hashCode() {
        int result = names.hashCode();
        result = 31 * result + room.hashCode();
        result = 31 * result + dimensions.hashCode();
        return result;
    }
}
