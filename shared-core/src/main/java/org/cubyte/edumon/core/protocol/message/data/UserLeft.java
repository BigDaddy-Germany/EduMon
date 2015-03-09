package org.cubyte.edumon.core.protocol.message.data;

import org.cubyte.edumon.core.protocol.message.Message;

public class UserLeft implements Message
{
    private String name;

    public UserLeft()
    {
    }

    public UserLeft(String name)
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
