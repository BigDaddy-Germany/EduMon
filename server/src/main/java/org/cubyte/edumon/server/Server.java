package org.cubyte.edumon.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.cubyte.edumon.core.protocol.message.receiver.MessageReceiver;
import org.cubyte.edumon.server.http.routing.Router;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server
{
    private final Router router;
    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private InetSocketAddress boundAddress;
    private SSLContext sslContext;

    private final List<Object> connectedUsers;
    private final MessageReceiver receiver;

    public Server(Router router)
    {
        this.router = router;
        this.channel = null;
        this.boundAddress = null;
        this.sslContext = null;
        this.connectedUsers = new CopyOnWriteArrayList<>();
        this.receiver = new MessageReceiver();
    }

    void addOnlineUser(Object user)
    {
        this.connectedUsers.add(user);
    }

    void removeOnlineUser(Object user)
    {
        this.connectedUsers.remove(user);
    }

    public Router getRouter()
    {
        return router;
    }

    public InetSocketAddress getBoundAddress()
    {
        return boundAddress;
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

    public void start(SocketAddress address) throws Exception
    {
        if (this.isRunning())
        {
            throw new IllegalStateException("The server is already running!");
        }
        try
        {
            this.bossGroup = new NioEventLoopGroup();
            this.workerGroup = new NioEventLoopGroup();
            this.channel = new ServerBootstrap().group(this.bossGroup, this.workerGroup)
                                                .channel(NioServerSocketChannel.class)
                                                .childHandler(new ServerChannelInitializer(this))
                                                .bind(address)
                                                .sync()
                                                .channel();
        }
        catch (Exception e)
        {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            this.boundAddress = null;
            this.channel = null;

            throw e;
        }
    }

    public boolean isRunning()
    {
        return this.channel != null && this.channel.isOpen();
    }

    public void stop()
    {
        this.channel.close().awaitUninterruptibly(5000);
        this.bossGroup.shutdownGracefully().awaitUninterruptibly(5000);
        this.workerGroup.shutdownGracefully().awaitUninterruptibly(5000);
        this.boundAddress = null;
        this.channel = null;
    }
}
