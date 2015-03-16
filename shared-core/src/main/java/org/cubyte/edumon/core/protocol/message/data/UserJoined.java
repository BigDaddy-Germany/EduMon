package org.cubyte.edumon.core.protocol.message.data;

import org.cubyte.edumon.core.protocol.message.Message;

public class UserJoined implements Message
{
    private String name;

    public UserJoined()
    {
    }

    public UserJoined(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
