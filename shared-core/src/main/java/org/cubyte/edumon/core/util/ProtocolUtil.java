package org.cubyte.edumon.core.util;

import org.cubyte.edumon.core.protocol.crypto.TrustManagement;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.Security;

public class ProtocolUtil
{
    private ProtocolUtil()
    {
    }

    public static SSLContext loadSslContext()
    {
        String algo = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algo == null)
        {
            algo = "SunX509";
        }

        try
        {
            KeyStore keyStore = KeyStore.getInstance("JKS");

            InputStream stream = ProtocolUtil.class.getResourceAsStream("/testing.keystore");
            if (stream == null)
            {
                return null;
            }
            try
            {
                keyStore.load(stream, "sicher".toCharArray());
            }
            finally
            {
                stream.close();
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(algo);
            keyManagerFactory.init(keyStore, "sicher".toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), TrustManagement.getTrustManagers(), null);
            return sslContext;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static InetSocketAddress stringToInetSocketAddress(String string)
    {
        return stringToInetSocketAddress(string, 8080);
    }

    public static InetSocketAddress stringToInetSocketAddress(String string, int defaultPort)
    {
        string = string.trim();
        String host = string;
        int port = defaultPort;

        int colonOffset = string.indexOf(":");
        if (colonOffset > -1)
        {
            host = string.substring(0, colonOffset);
            port = Integer.parseInt(string.substring(colonOffset + 1));
        }


        return new InetSocketAddress(host, port);
    }
}
