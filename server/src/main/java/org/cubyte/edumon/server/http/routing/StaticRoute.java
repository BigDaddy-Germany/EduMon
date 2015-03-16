package org.cubyte.edumon.server.http.routing;

import io.netty.handler.codec.http.HttpMethod;
import org.cubyte.edumon.server.http.RequestHandler;

public class StaticRoute extends Route
{
    public StaticRoute(HttpMethod method, String route, RequestHandler handler)
    {
        super(method, route, handler);
    }

    @Override
    public boolean matches(String path)
    {
        return path.equals(this.getRoute());
    }
}
