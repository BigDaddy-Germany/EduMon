package org.cubyte.edumon.client.messaging.messagebody;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ThumbResult implements MessageBody {
    public final int id;
    public final float average;

    @JsonCreator
    public ThumbResult(@JsonProperty("id") int id, @JsonProperty("average") float average) {
        this.id = id;
        this.average = average;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThumbResult that = (ThumbResult) o;

        return Float.compare(that.average, average) == 0 && id == that.id;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (average != +0.0f ? Float.floatToIntBits(average) : 0);
        return result;
    }
}
