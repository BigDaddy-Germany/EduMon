package org.cubyte.edumon.core.protocol.serialization;

import io.netty.buffer.ByteBuf;

public interface Serializer<T>
{
    int serialize(T object, ByteBuf data) throws SerializationException;

    T deserialize(ByteBuf data) throws SerializationException;
}
