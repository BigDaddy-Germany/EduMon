package org.cubyte.edumon.client.messaging.messagebodies;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Sensordata extends MessageBody {
    public final int keys;
    public final int mdist;
    public final int mclicks;
    public final double volume;

    @JsonCreator
    public Sensordata(@JsonProperty("keys") int keys, @JsonProperty("mdist") int mdist,
                      @JsonProperty("mclicks") int mclicks, @JsonProperty("volume") double volume) {
        this.keys = keys;
        this.mdist = mdist;
        this.mclicks = mclicks;
        this.volume = volume;
    }
}
