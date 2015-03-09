package org.cubyte.edumon.server;

import org.cubyte.edumon.core.protocol.crypto.DummyTrustManager;
import org.cubyte.edumon.core.protocol.crypto.TrustManagement;
import org.cubyte.edumon.core.util.ProtocolUtil;
import org.cubyte.edumon.server.http.routing.Router;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        TrustManagement.registerTrustManager(new DummyTrustManager());
        Router router = new Router();
        Server server = new Server(router);
        server.setSslContext(ProtocolUtil.loadSslContext());

        int port = 8080;
        if (args.length > 0)
        {
            try
            {
                port = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                exitWithUsage("Port must be a number!");
            }
        }
        if (port > 65535 || port < 0)
        {
            exitWithUsage("The port post be between 0 and 65536");
        }

        try
        {
            server.start(new InetSocketAddress(InetAddress.getByAddress(new byte[] {0, 0, 0, 0}), port));
            System.out.println(router.toString());
        }
        catch (Exception ignored)
        {
            System.err.println("Failed to start the server!");
            ignored.printStackTrace(System.err);
        }
    }

    private static void exitWithUsage(String message)
    {
        String path;
        try
        {
            String pwd = new File(".").getCanonicalPath();
            String jar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getCanonicalPath();
            path = jar.replace(pwd + File.separator, "");
        }
        catch (Exception e)
        {
            path = "<jar file>";
        }
        System.err.println("Error: " + message);
        System.err.println("Usage: java -jar " + path + " [port]");
        System.exit(1);
    }
}
