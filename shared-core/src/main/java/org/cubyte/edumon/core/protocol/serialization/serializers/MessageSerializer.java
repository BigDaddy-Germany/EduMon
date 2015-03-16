package org.cubyte.edumon.core.protocol.serialization.serializers;

import org.cubyte.edumon.core.protocol.message.DataTypes;
import org.cubyte.edumon.core.protocol.message.Message;
import org.cubyte.edumon.core.protocol.message.Packet;
import org.cubyte.edumon.core.protocol.serialization.Serialization;
import org.cubyte.edumon.core.protocol.serialization.SerializationException;
import org.cubyte.edumon.core.protocol.serialization.Serializer;
import io.netty.buffer.ByteBuf;

public class MessageSerializer implements Serializer<Packet>
{
    @Override
    public int serialize(Packet object, ByteBuf data) throws SerializationException
    {
        data.writeByte(object.getFlags());
        data.writeShort(object.getType() & 0xFFFF);
        data.writeShort(object.getVersion() & 0xFFFF);

        return 1 + 2 + 2 + Serialization.serialize(object.getData(), data);
    }

    @Override
    public Packet deserialize(ByteBuf data) throws SerializationException
    {
        byte flags = data.readByte();
        int type = data.readShort();
        int version = data.readShort();
        Message message = Serialization.deserialize(DataTypes.getDataClass(type, version), data);

        return new Packet(flags, type, version, message);
    }
}
