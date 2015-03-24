package org.cubyte.edumon.client.messaging.messagebody.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Dimensions {
    public final int width;
    public final int height;

    @JsonCreator
    public Dimensions(@JsonProperty("width") int width, @JsonProperty("height") int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dimensions that = (Dimensions) o;

        return height == that.height && width == that.width;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        return result;
    }
}
