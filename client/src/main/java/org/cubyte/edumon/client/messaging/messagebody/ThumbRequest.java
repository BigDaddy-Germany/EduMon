package org.cubyte.edumon.client.messaging.messagebody;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

public class ThumbRequest implements MessageBody {
    public enum FeedbackType {
        thumb, rating
    }

    public final int id;
    public final FeedbackType type;

    @JsonCreator
    public ThumbRequest(@JsonProperty("id") int id, @JsonProperty("type") FeedbackType type) {
        this.id = id;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThumbRequest that = (ThumbRequest) o;

        return id == that.id && type == that.type;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + type.hashCode();
        return result;
    }
}
