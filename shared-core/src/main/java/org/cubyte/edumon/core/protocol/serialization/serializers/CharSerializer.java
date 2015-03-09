package org.cubyte.edumon.core.protocol.serialization.serializers;

import org.cubyte.edumon.core.protocol.serialization.Serializer;
import io.netty.buffer.ByteBuf;

public class CharSerializer implements Serializer<Character>
{
    @Override
    public int serialize(Character object, ByteBuf data)
    {
        data.writeChar(object);
        return 1;
    }

    @Override
    public Character deserialize(ByteBuf data)
    {
        return data.readChar();
    }
}
