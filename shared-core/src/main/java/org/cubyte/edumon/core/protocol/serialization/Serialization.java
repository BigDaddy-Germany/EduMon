package org.cubyte.edumon.core.protocol.serialization;

import org.cubyte.edumon.core.protocol.serialization.serializers.*;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Serialization
{
    private static final Map<Class<?>, Serializer<?>> SERIALIZERS = new HashMap<Class<?>, Serializer<?>>();

    static
    {
        // Register primitive serializers
        registerSerializer(new ByteSerializer(), Byte.class, byte.class);
        registerSerializer(new ArraySerializer<Byte>(Byte.class), Byte[].class, byte[].class);
        registerSerializer(new ShortSerializer(), Short.class, short.class);
        registerSerializer(new IntegerSerializer(), Integer.class, int.class);
        registerSerializer(new LongSerializer(), Long.class, long.class);

        registerSerializer(new FloatSerializer(), Float.class, float.class);
        registerSerializer(new DoubleSerializer(), Double.class, double.class);

        registerSerializer(new CharSerializer(), Character.class, char.class);
        registerSerializer(new StringSerializer(), String.class);

        registerSerializer(new UUIDSerializer(), UUID.class);
    }

    public static synchronized void registerSerializer(Serializer<?> serializer, Class<?>... types)
    {
        assert types.length > 0 : "No types given!";

        for (Class<?> type : types)
        {
            SERIALIZERS.put(type, serializer);
        }
    }

    public static synchronized Serializer getSerializer(Class type)
    {
        Serializer serializer = SERIALIZERS.get(type);
        if (serializer == null)
        {
            for (Map.Entry<Class<?>, Serializer<?>> entry : SERIALIZERS.entrySet())
            {
                if (entry.getKey().isAssignableFrom(type))
                {
                    registerSerializer(serializer = entry.getValue(), type);
                    break;
                }
            }
        }
        if (serializer == null)
        {
            throw new IllegalArgumentException("Unknown type " + type.getName() + "! Did you forget to register the serializer?");
        }
        return serializer;
    }

    @SuppressWarnings("unchecked")
    public static int serialize(Object object, ByteBuf data) throws SerializationException
    {
        return getSerializer(object.getClass()).serialize(object, data);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(Class<T> type, ByteBuf data) throws SerializationException
    {
        return ((Serializer<T>)getSerializer(type)).deserialize(data);
    }
}
