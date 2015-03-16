package org.cubyte.edumon.server.http.routing;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import org.cubyte.edumon.server.http.WebRequest;
import org.cubyte.edumon.server.http.RequestHandler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Router
{
    private final ConcurrentMap<HttpMethod, RequestHandler> defaultHandlerMap;
    private final ConcurrentMap<HttpMethod, LinkedList<Route>> routes;

    public Router()
    {
        this.defaultHandlerMap = new ConcurrentHashMap<HttpMethod, RequestHandler>();
        this.routes = new ConcurrentHashMap<HttpMethod, LinkedList<Route>>();
    }

    public void setDefaultHandler(HttpMethod method, RequestHandler handler)
    {
        this.defaultHandlerMap.put(method, handler);
    }

    public RequestHandler getDefaultHandler(HttpMethod method)
    {
        return this.defaultHandlerMap.get(method);
    }

    public Route addRoute(HttpMethod method, String path, RequestHandler handler)
    {
        Route route = Route.parse(method, path, handler);
        LinkedList<Route> routeList = this.routes.get(method);
        if (routeList == null)
        {
            this.routes.put(method, routeList = new LinkedList<Route>());
        }
        routeList.add(route);
        return route;
    }

    public HttpResponse execute(WebRequest request) throws UnknownRouteException {
        LinkedList<Route> routeList = this.routes.get(request.getMethod());
        if (routeList == null)
        {
            throw new UnknownRouteException("No routes registered for method " + request.getMethod().name());
        }

        Iterator<Route> it = routeList.iterator();
        if (!it.hasNext())
        {
            throw new UnknownRouteException("No routes registered for method " + request.getMethod().name());
        }

        final String path = request.getPath();
        Route r;
        while (it.hasNext())
        {
            r = it.next();
            if (r.matches(path))
            {
                RequestHandler handler = r.getHandler();
                if (handler != null)
                {
                    handler.handle(request);
                }
            }
        }
        throw new UnknownRouteException("No route found for " + path + " and method " + request.getMethod().name());
    }

    @Override
    public String toString() {
        return "Router{routes=" + routes + '}';
    }
}
