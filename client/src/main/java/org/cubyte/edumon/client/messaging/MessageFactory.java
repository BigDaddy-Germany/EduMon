package org.cubyte.edumon.client.messaging;

import org.cubyte.edumon.client.messaging.messagebody.MessageBody;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageFactory {
    public final MessageQueue queue;
    public final String to;
    public final String room;

    public MessageFactory(MessageQueue queue, String to, String room) {
        this.queue = queue;
        this.to = to;
        this.room = room;
    }

    public Message create(MessageBody body) {
        return new Message(new Date(), queue.getSessionId(), to, room, body);
    }
}
