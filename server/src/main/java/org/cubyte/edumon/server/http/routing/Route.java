package org.cubyte.edumon.server.http.routing;

import io.netty.handler.codec.http.HttpMethod;
import org.cubyte.edumon.server.http.RequestHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public abstract class Route
{
    private final HttpMethod method;
    private final String route;
    private final AtomicReference<RequestHandler> handler;

    public Route(HttpMethod method, String route, RequestHandler handler)
    {
        assert method != null : "The method my not be null!";
        assert route != null : "The route my not be null!";

        this.method = method;
        this.route = route;
        this.handler = new AtomicReference<RequestHandler>(handler);
    }

    public static Route parse(HttpMethod method, String route, RequestHandler handler)
    {
        StringBuilder regex = new StringBuilder("^");

        final char[] chars = route.toCharArray();
        final int len = chars.length;

        int argCounter = 1;
        StringBuilder name;
        Map<Integer, String> groupNameMap = new HashMap<>();
        boolean dynamic = false;

        for (int i = 0; i < len; ++i)
        {
            switch (chars[i])
            {
                case '\\':
                    break;
                case ':': // single segment regex
                    regex.append("(?<");
                    while (i < len)
                    {
                        if (chars[i] != '/')
                        {
                            regex.append(chars[i]);
                            i++;
                        }
                        else
                        {
                            break;
                        }
                    }
                    regex.append(">[^/]+)");
                    break;
                case '*': // multi segment regex
                    regex.append("(?<");
                    while (i < len)
                    {
                        regex.append(chars[i]);
                        i++;
                    }
                    regex.append(regex.append(">.*)"));
                    break;
                case '$': // raw regex
                    regex.append("(?<");
                    // TODO append name
                    regex.append('>');
                    // TODO append regex
                    regex.append(')');
                    break;
                default:
                    regex.append(chars[i]);
            }
        }

        if (dynamic)
        {
            return new DynamicRoute(method, route, handler, Pattern.compile("^" + regex.append('$').toString()), groupNameMap);
        }
        else
        {
            return new StaticRoute(method, route, handler);
        }
    }

    public HttpMethod getMethod()
    {
        return method;
    }

    public String getRoute()
    {
        return route;
    }

    public RequestHandler getHandler()
    {
        return this.handler.get();
    }

    public void setHandler(RequestHandler handler)
    {
        this.handler.set(handler);
    }

    public abstract boolean matches(String path);

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Route))
        {
            return false;
        }

        Route route1 = (Route)o;

        if (!method.equals(route1.method))
        {
            return false;
        }
        if (!route.equals(route1.route))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = method.hashCode();
        result = 31 * result + route.hashCode();
        return result;
    }
}
