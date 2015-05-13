package org.cubyte.edumon.client.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * @author Jonas
 */
public class Response {
    public final ArrayList<Message> inbox;
    public final ArrayList<String> errorMessages;
    public final String clientId;

    @JsonCreator
    public Response(@JsonProperty("inbox") ArrayList<Message> inbox, @JsonProperty("errorMessages") ArrayList<String> errorMessages,
                    @JsonProperty("clientId") String clientId) {
        this.inbox = inbox;
        this.errorMessages = errorMessages;
        this.clientId = clientId;
    }
}
