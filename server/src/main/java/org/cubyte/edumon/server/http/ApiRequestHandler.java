package org.cubyte.edumon.server.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

public class ApiRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest>
{
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception
    {

    }
}
