package org.cubyte.edumon.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import org.cubyte.edumon.core.protocol.MessageCodec;

public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final ClientHandshakeHandler handShaker;

    public ClientChannelInitializer(ClientHandshakeHandler handShaker) {
        this.handShaker = handShaker;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new MessageCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
        pipeline.addLast(this.handShaker);
    }
}
