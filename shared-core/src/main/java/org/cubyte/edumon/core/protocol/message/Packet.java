package org.cubyte.edumon.core.protocol.message;

public class Packet
{
    public static final int NONE = 0;
    public static final int COMPRESSED = 1;
    public static final int ENCRYPTED = 2;
    public static final int RESERVED_1 = 4;
    public static final int RESERVED_2 = 8;
    public static final int RESERVED_3 = 16;
    public static final int RESERVED_4 = 32;
    public static final int RESERVED_5 = 64;
    public static final int RESERVED_6 = 128;

    private final byte flags;
    private final int type;
    private final int version;
    private final Message data;

    public Packet(int flags, int type, int version, Message data)
    {
        this.flags = (byte)flags;
        this.type = type;
        this.version = version;
        this.data = data;
    }

    public static Packet create(Message data)
    {
        short[] type = DataTypes.getType(data.getClass());
        if (type == DataTypes.NOT_FOUND)
        {
            throw new RuntimeException("Message type not registered: " + data.getClass().getName());
        }
        return new Packet(NONE, type[0], type[1], data);
    }

    public static Packet create(int flags, Message data)
    {
        short[] type = DataTypes.getType(data.getClass());
        return new Packet(flags, type[0], type[1], data);
    }

    public byte getFlags()
    {
        return flags;
    }

    public boolean is(int flags)
    {
        return (this.flags & flags) == flags;
    }

    public int getType()
    {
        return type;
    }

    public int getVersion()
    {
        return version;
    }

    public Message getData()
    {
        return data;
    }

    @Override
    public String toString()
    {
        return "Message{" +
                "flags=" + flags +
                ", type=" + type +
                ", version=" + version +
                ", data=" + data +
                '}';
    }
}
