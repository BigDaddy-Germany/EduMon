package org.cubyte.edumon.core.protocol;

import io.netty.channel.Channel;
import org.cubyte.edumon.core.protocol.message.Packet;
import org.cubyte.edumon.core.protocol.message.receiver.MessageReceiver;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;

public class ClientToServerConnection extends AbstractConnection {
    private final MessageReceiver receiver;

    public ClientToServerConnection(Channel channel, MessageReceiver receiver) {
        super(channel);
        this.receiver = receiver;
    }

    public MessageReceiver getReceiver() {
        return receiver;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    protected void handleMessage(ChannelHandlerContext ctx, Packet msg) {
        if (!this.receiver.receiveMessage(msg)) {
            System.out.println("Unhandled message: " + msg.getType());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace(System.err);

        ctx.close();
    }

    @Override
    public ChannelFuture closeWith(int reason, String text) {
        return channel().writeAndFlush(new CloseWebSocketFrame(reason, text));
    }
}
