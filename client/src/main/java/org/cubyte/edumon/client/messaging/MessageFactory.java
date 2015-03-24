package org.cubyte.edumon.client.messaging;

import org.cubyte.edumon.client.messaging.messagebody.MessageBody;

import java.util.Date;

public class MessageFactory {
    public int id; //TODO Atomic Integer
    public final String from;
    public final String to;
    public final String room;

    public MessageFactory(int id, String from, String to, String room) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.room = room;
    }

    public Message create(MessageBody body) {
        return new Message(id++, new Date(), from, to, room, body);
    }
}
