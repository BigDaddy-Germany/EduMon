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
    static final String HTTP_CODEC_NAME     = "http-codec";
    static final String AGGREGATOR_NAME     = "aggregator";
    static final String MESSAGE_CODEC_NAME  = "message-codec";
    static final String HANDLER_NAME        = "handler";

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
        pipeline.addLast(HTTP_CODEC_NAME, new HttpServerCodec());
        pipeline.addLast(MESSAGE_CODEC_NAME, new MessageCodec());
        pipeline.addLast(AGGREGATOR_NAME, new HttpObjectAggregator(8192));
        pipeline.addLast(HANDLER_NAME, new ServerHandshakeHandler(this.server));
    }
}
