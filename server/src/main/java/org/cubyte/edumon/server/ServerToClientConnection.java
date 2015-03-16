package org.cubyte.edumon.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.cubyte.edumon.core.protocol.AbstractConnection;
import org.cubyte.edumon.core.protocol.message.Message;
import org.cubyte.edumon.core.protocol.message.Packet;
import org.cubyte.edumon.core.protocol.message.data.SentMessage;

public class ServerToClientConnection extends AbstractConnection {
    private final Server server;
    private final WebSocketServerHandshaker handShaker;

    public ServerToClientConnection(Channel channel, Server server, WebSocketServerHandshaker handShaker) {
        super(channel);
        this.server = server;
        this.handShaker = handShaker;
    }

    public Server getServer() {
        return server;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead0(ctx, msg);
        System.out.println(msg.getClass().getName());
    }

    @Override
    protected void handleMessage(ChannelHandlerContext ctx, Packet packet) {
        Message m = packet.getData();
        if (m instanceof SentMessage)
        {
            SentMessage sm = (SentMessage) m;
            System.out.println(sm.getName() + " -> " + sm.getMessage());
        }
    }

    @Override
    public ChannelFuture closeWith(int reason, String message) {
        return this.handShaker.close(channel(), new CloseWebSocketFrame(reason, message));
    }
}
