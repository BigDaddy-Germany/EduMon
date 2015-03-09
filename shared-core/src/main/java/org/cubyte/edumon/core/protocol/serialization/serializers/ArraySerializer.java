package org.cubyte.edumon.core.protocol.serialization.serializers;

import org.cubyte.edumon.core.protocol.serialization.Serialization;
import org.cubyte.edumon.core.protocol.serialization.SerializationException;
import org.cubyte.edumon.core.protocol.serialization.Serializer;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Array;

public class ArraySerializer<T> implements Serializer<T[]>
{
    private final Class<T> type;

    public ArraySerializer(Class<T> type)
    {
        this.type = type;
    }

    @Override
    public int serialize(T[] object, ByteBuf data) throws SerializationException
    {
        data.writeInt(object.length);
        int bytesWritten = 4;
        for (T entry : object)
        {
            bytesWritten += Serialization.serialize(entry, data);
        }

        return bytesWritten;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] deserialize(ByteBuf data) throws SerializationException
    {
        T[] array = (T[])Array.newInstance(this.type, data.readInt());
        for (int i = 0; i < array.length; ++i)
        {
            array[i] = Serialization.deserialize(this.type, data);
        }
        return array;
    }
}
