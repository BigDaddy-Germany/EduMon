package org.cubyte.edumon.server.http.routing;

public class UnknownRouteException extends Exception
{
    public UnknownRouteException(String message)
    {
        super(message);
    }
}
