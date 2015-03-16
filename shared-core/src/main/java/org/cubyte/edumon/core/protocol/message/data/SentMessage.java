package org.cubyte.edumon.core.protocol.message.data;

import org.cubyte.edumon.core.protocol.message.Message;

public class SentMessage implements Message
{
    public String name;
    public String message;

    public SentMessage()
    {
        this(null, null);
    }

    public SentMessage(String name, String message)
    {
        this.name = name;
        this.message = message;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
