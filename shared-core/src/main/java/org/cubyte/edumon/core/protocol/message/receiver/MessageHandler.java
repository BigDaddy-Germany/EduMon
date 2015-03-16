package org.cubyte.edumon.core.protocol.message.receiver;

import org.cubyte.edumon.core.protocol.message.Packet;

public interface MessageHandler
{
    boolean handle(Packet packet);
}
