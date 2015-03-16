package org.cubyte.edumon.core.protocol.serialization.serializers;

import org.cubyte.edumon.core.protocol.serialization.Serializer;
import io.netty.buffer.ByteBuf;

public class ByteSerializer implements Serializer<Byte>
{
    @Override
    public int serialize(Byte object, ByteBuf data)
    {
        data.writeByte(object);
        return 1;
    }

    @Override
    public Byte deserialize(ByteBuf data)
    {
        return data.readByte();
    }
}
