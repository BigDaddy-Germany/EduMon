package org.cubyte.edumon.client.messaging.messagebodies;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Sensordata implements MessageBody {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sensordata that = (Sensordata) o;

        if (keys != that.keys) return false;
        if (mclicks != that.mclicks) return false;
        if (mdist != that.mdist) return false;
        if (Double.compare(that.volume, volume) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = keys;
        result = 31 * result + mdist;
        result = 31 * result + mclicks;
        temp = Double.doubleToLongBits(volume);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
