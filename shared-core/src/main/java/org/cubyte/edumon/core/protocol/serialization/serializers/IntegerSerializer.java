package org.cubyte.edumon.core.protocol.serialization.serializers;

import org.cubyte.edumon.core.protocol.serialization.Serializer;
import io.netty.buffer.ByteBuf;

public class IntegerSerializer implements Serializer<Integer>
{
    @Override
    public int serialize(Integer object, ByteBuf data)
    {
        data.writeInt(object);
        return 4;
    }

    @Override
    public Integer deserialize(ByteBuf data)
    {
        return data.readInt();
    }
}
