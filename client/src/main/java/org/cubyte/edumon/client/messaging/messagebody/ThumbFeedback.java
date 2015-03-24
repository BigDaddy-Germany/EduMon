package org.cubyte.edumon.client.messaging.messagebody;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ThumbFeedback {
    public final int id;
    public final float thumb;

    @JsonCreator
    public ThumbFeedback(@JsonProperty("id") int id, @JsonProperty("thumb") float thumb) {
        this.id = id;
        this.thumb =thumb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThumbFeedback that = (ThumbFeedback) o;

        return id == that.id && Float.compare(that.thumb, thumb) == 0;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (thumb != +0.0f ? Float.floatToIntBits(thumb) : 0);
        return result;
    }
}
