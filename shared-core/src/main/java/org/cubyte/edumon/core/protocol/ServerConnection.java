package org.cubyte.edumon.core.protocol;

import org.cubyte.edumon.core.protocol.message.Packet;
import org.cubyte.edumon.core.protocol.message.receiver.MessageReceiver;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.util.CharsetUtil;

public class ServerConnection extends AbstractConnection
{
    private final WebSocketClientHandshaker handshaker;
    private final MessageReceiver receiver;
    private ChannelPromise handshakeFuture;

    public ServerConnection(WebSocketClientHandshaker handshaker, MessageReceiver receiver)
    {
        this.handshaker = handshaker;
        this.receiver = receiver;
    }

    public MessageReceiver getReceiver()
    {
        return receiver;
    }

    public ChannelFuture shakeHand()
    {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception
    {
        super.handlerAdded(ctx);
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete())
        {
            handshaker.finishHandshake(ch, (FullHttpResponse)msg);
            System.out.println("WebSocket Client connected!");
            handshakeFuture.setSuccess();
        }
        else if (msg instanceof FullHttpResponse)
        {
            FullHttpResponse response = (FullHttpResponse)msg;
            System.out.println("Unexpected FullHttpResponse (getStatus=" + response.getStatus() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }
        else
        {
            super.channelRead0(ctx, msg);
        }
    }

    @Override
    protected void handleMessage(ChannelHandlerContext ctx, Packet msg)
    {
        if (!this.receiver.receiveMessage(msg))
        {
            System.out.println("Unhandled message: " + msg.getType());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        cause.printStackTrace();

        if (!handshakeFuture.isDone())
        {
            handshakeFuture.setFailure(cause);
        }

        ctx.close();
    }

    public ChannelFuture disconnect(int reason, String text)
    {
        return this.getContext().channel().writeAndFlush(new CloseWebSocketFrame(reason, text));
    }
}
