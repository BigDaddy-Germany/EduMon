package org.cubyte.edumon.core.protocol.serialization.serializers;

import org.cubyte.edumon.core.protocol.serialization.Serializer;
import io.netty.buffer.ByteBuf;

public class LongSerializer implements Serializer<Long>
{
    @Override
    public int serialize(Long object, ByteBuf data)
    {
        data.writeLong(object);
        return 8;
    }

    @Override
    public Long deserialize(ByteBuf data)
    {
        return data.readLong();
    }
}
