package org.cubyte.edumon.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.cubyte.edumon.core.protocol.DisconnectReason;
import org.cubyte.edumon.core.protocol.ClientToServerConnection;
import org.cubyte.edumon.core.protocol.message.Message;
import org.cubyte.edumon.core.protocol.message.Packet;
import org.cubyte.edumon.core.protocol.message.data.SentMessage;
import org.cubyte.edumon.core.protocol.message.receiver.MessageReceiver;

import java.net.InetSocketAddress;
import java.net.URI;

import static io.netty.handler.codec.http.HttpHeaders.EMPTY_HEADERS;
import static io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory.newHandshaker;
import static io.netty.handler.codec.http.websocketx.WebSocketVersion.V13;

public class Client
{
    private ClientToServerConnection connection;
    private final MessageReceiver receiver;

    public Client() {
        this.receiver = new MessageReceiver();
    }

    public MessageReceiver getReceiver() {
        return receiver;
    }

    public synchronized ClientToServerConnection connect(InetSocketAddress address) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();

            final String protocol = "ws";
            final int defaultPort = 80;

            String uri = protocol + "://" + address.getHostName();
            if (address.getPort() != defaultPort)
            {
                uri += ":" + address.getPort();
            }
            uri += "/";

            final WebSocketClientHandshaker handShaker = newHandshaker(URI.create(uri), V13, null, false, EMPTY_HEADERS);
            final ClientHandshakeHandler handShakeHandler = new ClientHandshakeHandler(receiver, handShaker);

            b.group(group).channel(NioSocketChannel.class).handler(new ClientChannelInitializer(handShakeHandler));

            System.out.println("WebSocket Client connecting");
            final Channel channel = b.connect(address).sync().channel();
            handShakeHandler.future().sync();
            this.connection = handShakeHandler.connection();
            return this.connection;
        } finally {
            group.shutdownGracefully();
        }
    }

    public boolean isConnected() {
        if (this.connection == null) {
            return false;
        }
        if (!this.connection.isConnected()) {
            this.connection = null;
            return false;
        }
        return true;
    }

    public ChannelFuture disconnect(int reason, String text) {

        return this.disconnect0(reason, text);
    }

    public ChannelFuture disconnect(DisconnectReason reason, String text) {
        return this.disconnect0(reason.getCode(), text);
    }

    private ChannelFuture disconnect0(int reason, String text) {
        System.out.println("WebSocket Client sending close");

        return this.connection.closeWith(reason, text);
    }

    public ChannelFuture sendPacket(Packet packet) {
        return this.connection.sendPacket(packet);
    }

    public ChannelFuture sendMessage(Message message) {
        return this.connection.sendMessage(message);
    }

    public static void main(String[] args) {
        Client client = new Client();

        try {
            final ClientToServerConnection c = client.connect(new InetSocketAddress("localhost", 8080));

            c.sendMessage(new SentMessage("It's me!", "test"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
