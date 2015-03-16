package org.cubyte.edumon.core.protocol.serialization;

import java.util.HashMap;
import java.util.Map;

public class SerializationUtil
{
    private static Map<Class<?>, Integer> primitiveSizes = new HashMap<>();
    private static byte[] ZERO = new byte[] {(byte)0};

    static
    {
        primitiveSizes.put(byte.class, 1);
        primitiveSizes.put(short.class, 2);
        primitiveSizes.put(int.class, 4);
        primitiveSizes.put(long.class, 8);
        primitiveSizes.put(float.class, 4);
        primitiveSizes.put(double.class, 8);
        primitiveSizes.put(char.class, 2);
    }

    public static int getPrimitiveSize(Class<?> type)
    {
        assert type != null : "The type must not be null!";
        assert type.isPrimitive() : "The given type is not primitive!";
        assert primitiveSizes.containsKey(type) : "Unknown type!";

        return primitiveSizes.get(type);
    }

    private static final char[] hexCharSet = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++)
        {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexCharSet[v >>> 4];
            hexChars[j * 2 + 1] = hexCharSet[v & 0x0F];
        }
        return new String(hexChars);
    }
}
