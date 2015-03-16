package org.cubyte.edumon.server;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.cubyte.edumon.core.protocol.message.Packet;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.handler.codec.http.HttpHeaders.Names.UPGRADE;
import static io.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static org.cubyte.edumon.server.ServerChannelInitializer.HANDLER_NAME;

public class ServerHandshakeHandler extends SimpleChannelInboundHandler<Object> {
    private final Server server;
    private WebSocketServerHandshaker handShaker;

    public ServerHandshakeHandler(Server server) {
        super(false);
        this.server = server;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("An uncaught exception was thrown: " + cause.getLocalizedMessage());
        cause.printStackTrace(System.err);

        ctx.channel().close().syncUninterruptibly();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object object) throws Exception {
        if (object instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest)object;
            if (isWebSocketRequest(request)) {
                this.setupConnection(ctx, request);
                request.release();
            } else {
                ctx.channel().writeAndFlush(new DefaultFullHttpResponse(HTTP_1_0, OK)).addListener(CLOSE);
            }
        } else if (object instanceof Packet) {
            handleMessage((Packet) object);
        }
    }

    protected void handleMessage(Packet msg) {
        if (!this.server.getReceiver().receiveMessage(msg)) {
            System.out.println("Unhandled message: " + msg.getType());
        }
    }

    private static boolean isWebSocketRequest(FullHttpRequest request) {
        final HttpHeaders headers = request.headers();
        return headers.contains(UPGRADE) && headers.get(UPGRADE).equalsIgnoreCase(WEBSOCKET);
    }

    private void setupConnection(final ChannelHandlerContext ctx, FullHttpRequest request) {
        String host = request.headers().get(HttpHeaders.Names.HOST);
        if (host == null) {
            InetSocketAddress address = this.server.getBoundAddress();
            host = address.getHostName() + ':' + address.getPort();
        }
        String protocol = "ws";
        if (request.getProtocolVersion().protocolName().equalsIgnoreCase("https")) {
            protocol += "s";
        }
        String location = protocol + "://" + host + request.getUri();

        final Channel ch = ctx.channel();

        // Handshake
        try {
            WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(location, null, false);
            this.handShaker = factory.newHandshaker(request);
            if (this.handShaker == null)
            {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            }
            else
            {
                this.handShaker.handshake(ctx.channel(), request).awaitUninterruptibly(500, TimeUnit.MILLISECONDS);
            }
        } catch (WebSocketHandshakeException e) {
            System.err.println("Failed to do the WebSocket handshake: " + e.getLocalizedMessage());
            e.printStackTrace(System.err);
            return;
        }

        System.out.println("User connected: " + ch.remoteAddress().toString());

        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.remove(this);
        //pipeline.remove(AGGREGATOR_NAME);
        pipeline.addLast(HANDLER_NAME, new ServerToClientConnection(ch, server, handShaker));
        System.out.println("Pipeline adjusted");
    }
}
