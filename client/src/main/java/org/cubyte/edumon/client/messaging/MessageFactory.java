package org.cubyte.edumon.client.messaging;

import org.cubyte.edumon.client.Main;
import org.cubyte.edumon.client.messaging.messagebody.MessageBody;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageFactory {
    private final Main owner;
    private final String to;

    public MessageFactory(Main owner, String to) {
        this.owner = owner;
        this.to = to;
    }

    public Message create(MessageBody body) {
        return new Message(new Date(), owner.getQueue().getSessionId(), to, owner.getRoom(), body);
    }
}
