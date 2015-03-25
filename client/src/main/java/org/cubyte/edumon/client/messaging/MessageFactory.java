package org.cubyte.edumon.client.messaging;

import org.cubyte.edumon.client.messaging.messagebody.MessageBody;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageFactory {
    public AtomicInteger id;
    public final String from;
    public final String to;
    public final String room;

    public MessageFactory(int id, String from, String to, String room) {
        this.id = new AtomicInteger(id);
        this.from = from;
        this.to = to;
        this.room = room;
    }

    public Message create(MessageBody body) {
        return new Message(id.getAndIncrement(), new Date(), from, to, room, body);
    }
}
