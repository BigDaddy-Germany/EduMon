package org.cubyte.edumon.client.messaging.messagebody;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ThumbResult implements MessageBody {
    public final float average;

    @JsonCreator
    public ThumbResult(@JsonProperty("average") float average) {
        this.average = average;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThumbResult that = (ThumbResult) o;

        return Float.compare(that.average, average) == 0;
    }

    @Override
    public int hashCode() {
        return (average != +0.0f ? Float.floatToIntBits(average) : 0);
    }
}
