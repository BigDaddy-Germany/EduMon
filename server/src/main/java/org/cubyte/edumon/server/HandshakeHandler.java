package org.cubyte.edumon.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.cubyte.edumon.core.protocol.DisconnectReason;
import org.cubyte.edumon.core.protocol.message.Message;
import org.cubyte.edumon.core.protocol.message.Packet;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaders.Names.UPGRADE;
import static io.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;

public class HandshakeHandler extends SimpleChannelInboundHandler<Object>
{
    private final Server server;
    private ScheduledFuture<?> loginTimeout;
    private WebSocketServerHandshaker handshaker;

    public HandshakeHandler(Server server)
    {
        super(false);
        this.server = server;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        System.err.println("An uncaught exception was thrown: " + cause.getLocalizedMessage());
        cause.printStackTrace(System.err);

        ctx.channel().close().syncUninterruptibly();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object object) throws Exception
    {
        if (object instanceof FullHttpRequest)
        {
            FullHttpRequest request = (FullHttpRequest)object;
            if (isWebSocketRequest(request))
            {
                this.setupConnection(ctx, request);
                request.release();
            }
        }
        else if (object instanceof Packet)
        {
            Message message = ((Packet)object).getData();

            this.close(ctx.channel());
        }
    }

    protected void handleMessage(Packet msg)
    {
        if (!this.server.getReceiver().receiveMessage(msg))
        {
            System.out.println("Unhandled message: " + msg.getType());
        }
    }

    private static boolean isWebSocketRequest(FullHttpRequest request)
    {
        final HttpHeaders headers = request.headers();
        return headers.contains(UPGRADE) && headers.get(UPGRADE).equalsIgnoreCase(WEBSOCKET);
    }

    private void setupConnection(final ChannelHandlerContext ctx, FullHttpRequest request)
    {
        String host = request.headers().get(HttpHeaders.Names.HOST);
        if (host == null)
        {
            InetSocketAddress address = this.server.getBoundAddress();
            host = address.getHostName() + ':' + address.getPort();
        }
        String protocol = "ws";
        if (request.getProtocolVersion().protocolName().equalsIgnoreCase("https"))
        {
            protocol += "s";
        }
        String location = protocol + "://" + host + request.getUri();

        // Handshake
        try
        {
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(location, null, false);
            this.handshaker = wsFactory.newHandshaker(request);
            if (this.handshaker == null)
            {
                WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
            }
            else
            {
                this.handshaker.handshake(ctx.channel(), request).awaitUninterruptibly(500, TimeUnit.MILLISECONDS);
            }
        }
        catch (WebSocketHandshakeException e)
        {
            System.err.println("Failed to do the websocket handshake: " + e.getLocalizedMessage());
            e.printStackTrace(System.err);
            return;
        }

        System.out.println("User connected: " + ctx.channel().remoteAddress().toString());
        this.loginTimeout = ctx.executor().schedule(new Runnable()
        {
            @Override
            public void run()
            {
                close(ctx.channel());
            }
        }, 2, TimeUnit.SECONDS);
    }

    private void close(Channel ch)
    {
        System.out.println("Login timed out for " + ch.remoteAddress());
        handshaker.close(ch, new CloseWebSocketFrame(DisconnectReason.POLICY_VIOLATION.getCode(), "Login timed out."));
    }
}
