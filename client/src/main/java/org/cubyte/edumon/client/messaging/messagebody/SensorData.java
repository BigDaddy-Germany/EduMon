package org.cubyte.edumon.client.messaging.messagebody;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SensorData implements MessageBody {
    public final int keys;
    public final double mdist;
    public final int mclicks;
    public final double volume;

    @JsonCreator
    public SensorData(@JsonProperty("keys") int keys, @JsonProperty("mdist") double mdist,
                      @JsonProperty("mclicks") int mclicks, @JsonProperty("volume") double volume) {
        this.keys = keys;
        this.mdist = mdist;
        this.mclicks = mclicks;
        this.volume = volume;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorData that = (SensorData) o;

        return keys == that.keys && mclicks == that.mclicks &&
                mdist == that.mdist && Double.compare(that.volume, volume) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = keys;
        temp = Double.doubleToLongBits(mdist);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + mclicks;
        temp = Double.doubleToLongBits(volume);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
