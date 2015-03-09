package org.cubyte.edumon.server.http.routing;

import io.netty.handler.codec.http.HttpMethod;
import org.cubyte.edumon.server.http.RequestHandler;

import java.util.Map;
import java.util.regex.Pattern;

public class DynamicRoute extends Route
{
    private final Pattern pattern;
    private final Map<Integer, String> groupNameMap;

    public DynamicRoute(HttpMethod method, String route, RequestHandler handler, Pattern pattern, Map<Integer, String> groupNameMap)
    {
        super(method, route, handler);
        this.pattern = pattern;
        this.groupNameMap = groupNameMap;
    }

    @Override
    public boolean matches(String path)
    {
        return this.pattern.matcher(path).find();
    }
}
