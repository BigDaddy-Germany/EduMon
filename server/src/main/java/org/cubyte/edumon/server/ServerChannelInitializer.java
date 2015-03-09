package org.cubyte.edumon.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;
import org.cubyte.edumon.core.protocol.MessageCodec;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel>
{
    private final Server server;

    public ServerChannelInitializer(Server server)
    {
        this.server = server;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception
    {
        ChannelPipeline pipeline = ch.pipeline();

        {
            SSLContext sslContext = this.server.getSslContext();
            if (sslContext != null)
            {
                SSLEngine sslEngine = sslContext.createSSLEngine();
                sslEngine.setUseClientMode(false);
                pipeline.addLast("ssl", new SslHandler(sslEngine));
            }
        }
        pipeline.addLast("http-codec", new HttpServerCodec());
        pipeline.addLast("message-codec", new MessageCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
        pipeline.addLast("handler", new HandshakeHandler(this.server));
    }
}
