package org.cubyte.edumon.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.cubyte.edumon.core.protocol.AbstractConnection;
import org.cubyte.edumon.core.protocol.DisconnectReason;
import org.cubyte.edumon.core.protocol.message.Message;
import org.cubyte.edumon.core.protocol.message.Packet;
import org.cubyte.edumon.core.protocol.message.data.SentMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientConnection extends AbstractConnection
{
    private final Server server;
    private final WebSocketServerHandshaker handshaker;

    public ClientConnection(Server server, WebSocketServerHandshaker handshaker)
    {
        this.server = server;
        this.handshaker = handshaker;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {

    }

    public void disconnect(DisconnectReason reason, String message)
    {
        this.handshaker.close(this.getContext().channel(), new CloseWebSocketFrame(reason.getCode(), message));
    }

    @Override
    protected void handleMessage(ChannelHandlerContext ctx, Packet packet)
    {
        Message m = packet.getData();
        if (m instanceof SentMessage)
        {
            SentMessage sm = (SentMessage) m;
            System.out.println(sm.getName() + " -> " + sm.getMessage());
        }
    }
}
