package org.cubyte.edumon.core.protocol.serialization.serializers;

import org.cubyte.edumon.core.protocol.serialization.Serializer;
import io.netty.buffer.ByteBuf;

public class FloatSerializer implements Serializer<Float>
{
    @Override
    public int serialize(Float object, ByteBuf data)
    {
        data.writeFloat(object);
        return 4;
    }

    @Override
    public Float deserialize(ByteBuf data)
    {
        return data.readFloat();
    }
}
