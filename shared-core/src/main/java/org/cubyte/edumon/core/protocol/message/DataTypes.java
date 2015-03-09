package org.cubyte.edumon.core.protocol.message;

import org.cubyte.edumon.core.protocol.message.data.*;
import org.cubyte.edumon.core.protocol.serialization.Serialization;
import org.cubyte.edumon.core.protocol.serialization.serializers.ReflectiveObjectSerializer;

import java.util.HashMap;
import java.util.Map;

public class DataTypes
{
    public static short[] NOT_FOUND = new short[0];
    private static final Map<Integer, Class<? extends Message>> TYPE_CLASS = new HashMap<>();
    private static final Map<Class<? extends Message>, Integer> CLASS_TYPE = new HashMap<>();

    public static <T extends Message> void registerType(int type, int version, Class<T> clazz)
    {
        int combined = combine(type, version);
        TYPE_CLASS.put(combined, clazz);
        CLASS_TYPE.put(clazz, combined);
        Serialization.registerSerializer(new ReflectiveObjectSerializer<T>(clazz), clazz);
    }

    public static Class<? extends Message> getDataClass(int type, int version)
    {
        return TYPE_CLASS.get(combine(type, version));
    }

    public static int getIntType(Class<? extends Message> clazz)
    {
        if (CLASS_TYPE.containsKey(clazz))
        {
            return CLASS_TYPE.get(clazz);
        }
        return -1;
    }

    public static short[] getType(Class<? extends Message> clazz)
    {
        int type = getIntType(clazz);
        if (type >= 0)
        {
            return split(type);
        }
        return NOT_FOUND;
    }

    private static int combine(int type, int version)
    {
        return ((type & 0xFFFF) << 16) | (version & 0xFFFF);
    }

    private static short[] split(int combined)
    {
        return new short[] {(short)(combined >> 16), (short)(combined & 0xFFFF)};
    }

    static
    {
        int i = 0;
        registerType(++i, 1, ReceivedTextMessage.class);
        registerType(++i, 1, SentMessage.class);
        registerType(++i, 1, UserJoined.class);
        registerType(++i, 1, UserLeft.class);
    }

    public static boolean hasType(Class<? extends Message> messageData)
    {
        return CLASS_TYPE.containsKey(messageData);
    }
}
