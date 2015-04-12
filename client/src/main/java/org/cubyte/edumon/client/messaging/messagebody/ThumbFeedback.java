package org.cubyte.edumon.client.messaging.messagebody;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ThumbFeedback {
    public final int id;
    public final float value;

    @JsonCreator
    public ThumbFeedback(@JsonProperty("id") int id, @JsonProperty("value") float value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThumbFeedback that = (ThumbFeedback) o;

        return id == that.id && Float.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (value != +0.0f ? Float.floatToIntBits(value) : 0);
        return result;
    }
}
