package org.cubyte.edumon.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.cubyte.edumon.core.protocol.AbstractConnection;
import org.cubyte.edumon.core.protocol.DisconnectReason;
import org.cubyte.edumon.core.protocol.message.Packet;

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
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
