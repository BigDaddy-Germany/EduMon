package org.cubyte.edumon.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslHandler;
import org.cubyte.edumon.core.protocol.MessageCodec;
import org.cubyte.edumon.core.protocol.ServerConnection;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class ClientChannelInitializer extends ChannelInitializer<SocketChannel>
{
    private final SSLContext sslContext;
    private final ServerConnection connection;

    public ClientChannelInitializer(SSLContext sslContext, ServerConnection connection)
    {
        this.sslContext = sslContext;
        this.connection = connection;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception
    {
        ChannelPipeline pipeline = ch.pipeline();

        if (this.sslContext != null)
        {
            SSLEngine sslEngine = this.sslContext.createSSLEngine();
            sslEngine.setUseClientMode(true);
            pipeline.addLast("ssl", new SslHandler(sslEngine));
        }
        pipeline.addLast("http-codec", new HttpClientCodec());
        pipeline.addLast("message-codec", new MessageCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
        pipeline.addLast("handler", this.connection);
    }
}
