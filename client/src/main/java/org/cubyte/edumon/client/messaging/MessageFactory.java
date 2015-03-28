package org.cubyte.edumon.client.messaging;

import org.cubyte.edumon.client.messaging.messagebody.MessageBody;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageFactory {
    public final String from;
    public final String to;
    public final String room;

    public MessageFactory(String from, String to, String room) {
        this.from = from;
        this.to = to;
        this.room = room;
    }

    public Message create(MessageBody body) {
        return new Message(new Date(), from, to, room, body);
    }
}
