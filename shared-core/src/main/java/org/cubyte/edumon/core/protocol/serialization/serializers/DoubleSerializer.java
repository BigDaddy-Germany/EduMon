package org.cubyte.edumon.core.protocol.serialization.serializers;

import org.cubyte.edumon.core.protocol.serialization.Serializer;
import io.netty.buffer.ByteBuf;

public class DoubleSerializer implements Serializer<Double>
{
    @Override
    public int serialize(Double object, ByteBuf data)
    {
        data.writeDouble(object);
        return 8;
    }

    @Override
    public Double deserialize(ByteBuf data)
    {
        return data.readDouble();
    }
}
