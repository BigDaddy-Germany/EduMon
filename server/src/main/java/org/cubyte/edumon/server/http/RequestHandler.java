package org.cubyte.edumon.server.http;

import io.netty.handler.codec.http.HttpResponse;

public interface RequestHandler
{
    public HttpResponse handle(WebRequest request);
}
