package org.cubyte.edumon.core.protocol.crypto;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class DummyTrustManager implements X509TrustManager
{
    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException
    {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException
    {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        return null;
    }
}
