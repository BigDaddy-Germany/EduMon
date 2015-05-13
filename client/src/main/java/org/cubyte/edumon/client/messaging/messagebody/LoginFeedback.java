package org.cubyte.edumon.client.messaging.messagebody;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Jonas
 */
public class LoginFeedback implements MessageBody {
    public final int successCode;

    @JsonCreator
    public LoginFeedback(@JsonProperty("successCode") int successCode) {
        this.successCode = successCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LoginFeedback that = (LoginFeedback) o;

        return successCode == that.successCode;
    }

    @Override
    public int hashCode() {
        return successCode;
    }
}
