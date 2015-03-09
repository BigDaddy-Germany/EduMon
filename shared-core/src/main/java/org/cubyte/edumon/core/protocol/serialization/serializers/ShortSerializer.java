package org.cubyte.edumon.core.protocol.serialization.serializers;

import org.cubyte.edumon.core.protocol.serialization.Serializer;
import io.netty.buffer.ByteBuf;

public class ShortSerializer implements Serializer<Short>
{
    @Override
    public int serialize(Short object, ByteBuf data)
    {
        data.writeShort(object);
        return 2;
    }

    @Override
    public Short deserialize(ByteBuf data)
    {
        return data.readShort();
    }
}
