package org.cubyte.edumon.core.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.*;
import org.cubyte.edumon.core.protocol.message.Message;
import org.cubyte.edumon.core.protocol.message.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public abstract class AbstractConnection extends SimpleChannelInboundHandler<Object>
{
    private final Channel channel;

    public AbstractConnection(Channel channel) {
        this.channel = channel;
    }

    protected Channel channel() {
        return this.channel;
    }

    public boolean isConnected() {
        return channel().isOpen();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            this.handleWebSocketFrame(ctx, (WebSocketFrame)msg);
        } else if (msg instanceof Packet) {
            this.handleMessage(ctx, (Packet)msg);
        }
    }

    protected void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof PingWebSocketFrame) {
            System.out.println("Received ping");
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("Received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("Received close: " + ((CloseWebSocketFrame)frame).reasonText());
            ctx.channel().close();
        } else {
            System.err.println("Unexpected frame: " + frame.getClass().getName());
            if (frame instanceof TextWebSocketFrame) {
                System.out.println(((TextWebSocketFrame) frame).text());
            }
        }
    }

    public ChannelFuture ping(ByteBuf data) {
        return sendData(new PingWebSocketFrame(data));
    }

    public ChannelFuture ping() {
        return sendData(new PingWebSocketFrame());
    }

    public ChannelFuture pong(ByteBuf data) {
        return sendData(new PongWebSocketFrame(data));
    }

    public ChannelFuture pong() {
        return sendData(new PongWebSocketFrame());
    }

    protected ChannelFuture sendData(Object data) {
        return channel().writeAndFlush(data);
    }

    public ChannelFuture sendPacket(Packet packet) {
        return sendData(packet);
    }

    public ChannelFuture sendMessage(Message message) {
        return sendPacket(Packet.create(message));
    }

    protected abstract void handleMessage(ChannelHandlerContext ctx, Packet packet);

    protected abstract ChannelFuture closeWith(int reason, String message);

    public ChannelFuture disconnect(int reason, String message) {
        if (reason >= 4000 && reason < 5000) {
            throw new IllegalArgumentException("The code must be between 4000-4999");
        }
        return closeWith(reason, message);
    }

    public ChannelFuture disconnect(DisconnectReason reason, String message) {
        return closeWith(reason.getCode(), message);
    }
}
