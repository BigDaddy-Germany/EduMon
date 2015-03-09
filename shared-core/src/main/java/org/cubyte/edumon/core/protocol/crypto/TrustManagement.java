package org.cubyte.edumon.core.protocol.crypto;

import javax.net.ssl.TrustManager;
import java.util.concurrent.CopyOnWriteArraySet;

public class TrustManagement
{
    private static CopyOnWriteArraySet<TrustManager> trustManagers = new CopyOnWriteArraySet<TrustManager>();

    private TrustManagement()
    {
    }

    public static void registerTrustManager(TrustManager trustManager)
    {
        trustManagers.add(trustManager);
    }

    public static TrustManager[] getTrustManagers()
    {
        return trustManagers.toArray(new TrustManager[trustManagers.size()]);
    }
}
