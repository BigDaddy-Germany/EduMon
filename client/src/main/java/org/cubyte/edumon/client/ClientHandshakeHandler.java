package org.cubyte.edumon.client;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import org.cubyte.edumon.core.protocol.ClientToServerConnection;
import org.cubyte.edumon.core.protocol.message.receiver.MessageReceiver;

public class ClientHandshakeHandler extends SimpleChannelInboundHandler<Object> {
    private final MessageReceiver receiver;
    private final WebSocketClientHandshaker handShaker;
    private ChannelPromise handshakeFuture;
    private ClientToServerConnection connection;

    public ClientHandshakeHandler(MessageReceiver receiver, WebSocketClientHandshaker handShaker) {
        this.receiver = receiver;
        this.handShaker = handShaker;
    }

    public ChannelFuture future() {
        return handshakeFuture;
    }

    public ClientToServerConnection connection() {
        return this.connection;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handShaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("WebSocket Client disconnected during handshake!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        final Channel ch = ctx.channel();
        if (!handShaker.isHandshakeComplete()) {
            try {
                handShaker.finishHandshake(ch, (FullHttpResponse) msg);

                this.connection = new ClientToServerConnection(ch, this.receiver);
                final ChannelPipeline pipeline = ch.pipeline();
                pipeline.remove(this);
                pipeline.addLast(this.connection);

                handshakeFuture.setSuccess();

                System.out.println("WebSocket Client connected and handshake completed!");
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}
