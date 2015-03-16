package org.cubyte.edumon.core.protocol.serialization.serializers;

import org.cubyte.edumon.core.protocol.serialization.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

public class StringSerializer implements Serializer<String>
{
    private static final byte ETX = (byte)3;
    private final Charset charset;

    public StringSerializer()
    {
        this(CharsetUtil.UTF_8);
    }

    public StringSerializer(Charset charset)
    {
        this.charset = charset;
    }

    @Override
    public int serialize(String object, ByteBuf data)
    {
        if (object == null)
        {
            object = "";
        }
        byte[] rawString = object.getBytes(charset);
        data.writeBytes(rawString);
        data.writeByte(ETX);

        return rawString.length + 1;
    }

    @Override
    public String deserialize(ByteBuf data)
    {
        byte b;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        while ((b = data.readByte()) != ETX)
        {
            stream.write(b);
        }
        return new String(stream.toByteArray(), this.charset);
    }
}
