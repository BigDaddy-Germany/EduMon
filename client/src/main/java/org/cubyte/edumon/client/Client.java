package org.cubyte.edumon.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.cubyte.edumon.core.protocol.DisconnectReason;
import org.cubyte.edumon.core.protocol.ServerConnection;
import org.cubyte.edumon.core.protocol.message.Packet;
import org.cubyte.edumon.core.protocol.message.data.SentMessage;
import org.cubyte.edumon.core.protocol.message.receiver.MessageReceiver;

import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;

public class Client
{
    private ServerConnection connection;
    private SSLContext sslContext;
    private final MessageReceiver receiver;

    public Client()
    {
        this.receiver = new MessageReceiver();
    }

    public void setSslContext(SSLContext sslContext)
    {
        this.sslContext = sslContext;
    }

    public SSLContext getSslContext()
    {
        return sslContext;
    }

    public MessageReceiver getReceiver()
    {
        return receiver;
    }

    public synchronized void connect(InetSocketAddress address) throws Exception
    {
        EventLoopGroup group = new NioEventLoopGroup();
        try
        {
            Bootstrap b = new Bootstrap();

            SSLContext sslContext = getSslContext();
            String protocol = sslContext == null ? "ws" : "wss";
            final int defaultPort = sslContext == null ? 80 : 443;

            String uri = protocol + "://" + address.getHostName();
            if (address.getPort() != defaultPort)
            {
                uri += ":" + address.getPort();
            }
            uri += "/";


            HttpHeaders headers = DefaultHttpHeaders.EMPTY_HEADERS;

            final WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(URI.create(uri), WebSocketVersion.V13, null, false, headers);
            this.connection = new ServerConnection(handshaker, receiver);

            b.group(group).channel(NioSocketChannel.class).handler(new ClientChannelInitializer(sslContext, connection));

            System.out.println("WebSocket Client connecting");
            final Channel channel = b.connect(address).sync().channel();
            connection.shakeHand().sync();
        }
        finally
        {
            group.shutdownGracefully();
        }
    }

    public boolean isConnected()
    {
        if (this.connection == null)
        {
            return false;
        }
        if (!this.connection.isConnected())
        {
            this.connection = null;
            return false;
        }
        return true;
    }

    public ChannelFuture disconnect(int reason, String text)
    {
        assert reason >= 4000 && reason < 5000 : "The code must be between 4000-4999";

        return this.disconnect0(reason, text);
    }

    public ChannelFuture disconnect(DisconnectReason reason, String text)
    {
        return this.disconnect0(reason.getCode(), text);
    }

    private synchronized ChannelFuture disconnect0(int reason, String text)
    {
        System.out.println("WebSocket Client sending close");

        // WebSocketClientHandler will close the connection when the server
        // responds to the CloseWebSocketFrame.
        return this.connection.disconnect(reason, text);
    }

    public ChannelFuture sendMessage(Packet packet)
    {
        return this.connection.sendMessage(packet);
    }

    public static void main(String[] args)
    {
        Client c = new Client();

        try
        {
            c.connect(new InetSocketAddress("localhost", 8080));
            c.sendMessage(Packet.create(new SentMessage("It's me!", "test")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
