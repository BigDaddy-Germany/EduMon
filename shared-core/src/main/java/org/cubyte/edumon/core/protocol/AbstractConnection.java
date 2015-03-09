package org.cubyte.edumon.core.protocol;

import org.cubyte.edumon.core.protocol.message.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public abstract class AbstractConnection extends SimpleChannelInboundHandler<Object>
{
    private ChannelHandlerContext context = null;

    protected ChannelHandlerContext getContext()
    {
        return this.context;
    }

    public ChannelFuture sendMessage(Packet packet)
    {
        if (this.context == null)
        {
            throw new IllegalStateException("The channel has not been added yet!");
        }
        return this.context.channel().writeAndFlush(packet);
    }

    public boolean isConnected()
    {
        return this.context.channel().isOpen();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception
    {
        this.context = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if (msg instanceof WebSocketFrame)
        {
            this.handleWebSocketFrame(ctx, (WebSocketFrame)msg);
        }
        else if (msg instanceof Packet)
        {
            this.handleMessage(ctx, (Packet)msg);
        }
    }

    protected void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame)
    {
        if (frame instanceof PingWebSocketFrame)
        {
            System.out.println("Received ping");
        }
        else if (frame instanceof PongWebSocketFrame)
        {
            System.out.println("Received pong");
        }
        else if (frame instanceof CloseWebSocketFrame)
        {
            System.out.println("Received close: " + ((CloseWebSocketFrame)frame).reasonText());
            ctx.channel().close();
        }
        else
        {
            System.err.println("Unexpected frame: " + frame.getClass().getName());
        }
    }

    public void ping(ByteBuf data)
    {
        this.context.channel().writeAndFlush(new PingWebSocketFrame(data));
    }

    public void ping()
    {
        this.context.channel().writeAndFlush(new PingWebSocketFrame());
    }

    public void pong(ByteBuf data)
    {
        this.context.channel().writeAndFlush(new PongWebSocketFrame(data));
    }

    public void pong()
    {
        this.context.channel().writeAndFlush(new PongWebSocketFrame());
    }


    protected abstract void handleMessage(ChannelHandlerContext ctx, Packet packet);
}
