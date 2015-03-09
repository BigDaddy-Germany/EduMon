package org.cubyte.edumon.core.protocol.message.data;

import org.cubyte.edumon.core.protocol.message.Message;

public class ReceivedTextMessage implements Message
{
    private String message;
    private long sent;

    public ReceivedTextMessage()
    {
        this(null);
    }

    public ReceivedTextMessage(String message)
    {
        this(message, System.currentTimeMillis());
    }

    public ReceivedTextMessage(String message, long sent)
    {
        this.message = message;
        this.sent = sent;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public long getSent()
    {
        return sent;
    }

    public void setSent(long sent)
    {
        this.sent = sent;
    }

    @Override
    public String toString()
    {
        return "ReceivedTextMessageData{" +
                "message='" + message + '\'' +
                ", sent=" + sent +
                '}';
    }
}
