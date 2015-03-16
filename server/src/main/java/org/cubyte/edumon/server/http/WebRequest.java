package org.cubyte.edumon.server.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import org.cubyte.edumon.server.ServerToClientConnection;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class WebRequest
{
    private final FullHttpRequest request;
    private final ServerToClientConnection connection;
    private final String path;
    private final Map<String, List<String>> urlParameters;

    public WebRequest(FullHttpRequest request, ServerToClientConnection connection)
    {
        this.request = request;

        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri(), Charset.defaultCharset(), true, 100);
        this.path = decoder.path();
        this.urlParameters = decoder.parameters();

        this.connection = connection;
    }

    public HttpVersion getProtocolVersion()
    {
        return this.request.getProtocolVersion();
    }

    public HttpMethod getMethod()
    {
        return this.request.getMethod();
    }

    public ServerToClientConnection getConnection()
    {
        return connection;
    }

    public String getPath()
    {
        return path;
    }

    public Map<String, List<String>> getUrlParameters()
    {
        return urlParameters;
    }

    public HttpHeaders getHeaders()
    {
        return this.request.headers();
    }

    public ByteBuf getContent()
    {
        return this.request.content();
    }

    public FullHttpRequest getRawRequest()
    {
        return this.request;
    }
}
