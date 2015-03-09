package org.cubyte.edumon.core.protocol;

import org.cubyte.edumon.core.protocol.message.Packet;
import org.cubyte.edumon.core.protocol.serialization.Serialization;
import org.cubyte.edumon.core.protocol.serialization.serializers.MessageSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.nio.ByteOrder;
import java.util.List;

public class MessageCodec extends MessageToMessageCodec<BinaryWebSocketFrame, Packet>
{
    public MessageCodec()
    {
        Serialization.registerSerializer(new MessageSerializer(), Packet.class);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, List<Object> out) throws Exception
    {
        try
        {
            ByteBuf data = ctx.alloc().directBuffer().order(ByteOrder.LITTLE_ENDIAN);
            Serialization.serialize(msg, data);
            data.capacity(data.writerIndex());
            out.add(new BinaryWebSocketFrame(data));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out) throws Exception
    {
        try
        {
            out.add(Serialization.deserialize(Packet.class, msg.content().order(ByteOrder.LITTLE_ENDIAN)));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }
}
