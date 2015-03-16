package org.cubyte.edumon.core.protocol.serialization.serializers;

import org.cubyte.edumon.core.protocol.serialization.SerializationException;
import org.cubyte.edumon.core.protocol.serialization.Serializer;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class UUIDSerializer implements Serializer<UUID>
{
    @Override
    public int serialize(UUID object, ByteBuf data) throws SerializationException
    {
        data.writeLong(object.getMostSignificantBits());
        data.writeLong(object.getLeastSignificantBits());

        return 16;
    }

    @Override
    public UUID deserialize(ByteBuf data) throws SerializationException
    {
        return new UUID(data.readLong(), data.readLong());
    }
}
