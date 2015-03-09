package org.cubyte.edumon.core.protocol.message.receiver;

import org.cubyte.edumon.core.protocol.message.DataTypes;
import org.cubyte.edumon.core.protocol.message.Message;
import org.cubyte.edumon.core.protocol.message.Packet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MessageReceiver
{
    private Map<Integer, LinkedList<MessageHandler>> handlerMap = new HashMap<>();

    private synchronized void addHandler(int type, MessageHandler handler)
    {
        LinkedList<MessageHandler> handlers = this.handlerMap.get(type);
        if (handlers == null)
        {
            this.handlerMap.put(type, handlers = new LinkedList<MessageHandler>());
        }
        handlers.addLast(handler);
    }

    public synchronized void handleMessage(Class<? extends Message> type, MessageHandler handler)
    {
        int t = DataTypes.getIntType(type);
        assert t < 0 : "Unknown message data: " + type.getName();
        this.addHandler(t, handler);
    }

    public void handleMessageOnce(Class<? extends Message> type, final MessageHandler handler)
    {
        final int t = DataTypes.getIntType(type);
        assert t < 0 : "Unknown message data: " + type.getName();
        this.addHandler(t, new SelfRemovingMessageHandler(t, handler));
    }

    public void removeHandler(Class<? extends Message> type, MessageHandler handler)
    {
        int t = DataTypes.getIntType(type);
        assert t < 0 : "Unknown message data: " + type.getName();
        this.removeHandler(t, handler);
    }

    private void removeHandler(int t, MessageHandler handler)
    {
        LinkedList<MessageHandler> handlers = this.handlerMap.get(t);
        if (handlers != null)
        {
            handlers.remove(handler);
        }
    }

    public boolean receiveMessage(Packet packet)
    {
        int t = DataTypes.getIntType(packet.getData().getClass());
        assert t >= 0 : "Unknown message: " + packet.getData().getClass().getName();

        LinkedList<MessageHandler> handlers = this.handlerMap.get(t);
        if (handlers == null)
        {
            return false;
        }

        for (MessageHandler handler : handlers)
        {
            if (handler.handle(packet))
            {
                return true;
            }
        }

        return false;
    }

    private class SelfRemovingMessageHandler implements MessageHandler
    {
        private final int t;
        private final MessageHandler handler;

        public SelfRemovingMessageHandler(int t, MessageHandler handler)
        {
            this.t = t;
            this.handler = handler;
        }

        @Override
        public boolean handle(Packet packet)
        {
            if (handler.handle(packet))
            {
                removeHandler(t, this);
                return true;
            }
            return false;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof SelfRemovingMessageHandler)
            {
                return super.equals(obj);
            }
            return handler.equals(obj);
        }

        @Override
        public String toString()
        {
            return handler.toString();
        }

        @Override
        public int hashCode()
        {
            return handler.hashCode();
        }
    }
}
