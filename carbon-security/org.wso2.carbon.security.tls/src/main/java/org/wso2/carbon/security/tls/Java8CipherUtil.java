package org.wso2.carbon.security.tls;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Java8CipherUtil {

    private static List<String> java8Ciphers = new ArrayList<String>();

    public static final String TLS_DHE_RSA_WITH_AES_256_GCM_SHA384 = "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384";

    private static Log log = LogFactory.getLog(Java8CipherUtil.class);

    static {

        java8Ciphers.add("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384");
        java8Ciphers.add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384");
        java8Ciphers.add("TLS_RSA_WITH_AES_256_CBC_SHA256");
        java8Ciphers.add("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384");
        java8Ciphers.add("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384");
        java8Ciphers.add("TLS_DHE_RSA_WITH_AES_256_CBC_SHA256");
        java8Ciphers.add("TLS_DHE_DSS_WITH_AES_256_CBC_SHA256");
        java8Ciphers.add("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA");
        java8Ciphers.add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA");
        java8Ciphers.add("TLS_RSA_WITH_AES_256_CBC_SHA");
        java8Ciphers.add("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA");
        java8Ciphers.add("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA");
        java8Ciphers.add("TLS_DHE_RSA_WITH_AES_256_CBC_SHA");
        java8Ciphers.add("TLS_DHE_DSS_WITH_AES_256_CBC_SHA");
        java8Ciphers.add("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256");
        java8Ciphers.add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
        java8Ciphers.add("TLS_RSA_WITH_AES_128_CBC_SHA256");
        java8Ciphers.add("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256");
        java8Ciphers.add("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256");
        java8Ciphers.add("TLS_DHE_RSA_WITH_AES_128_CBC_SHA256");
        java8Ciphers.add("TLS_DHE_DSS_WITH_AES_128_CBC_SHA256");
        java8Ciphers.add("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA");
        java8Ciphers.add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA");
        java8Ciphers.add("TLS_RSA_WITH_AES_128_CBC_SHA");
        java8Ciphers.add("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA");
        java8Ciphers.add("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA");
        java8Ciphers.add("TLS_DHE_RSA_WITH_AES_128_CBC_SHA");
        java8Ciphers.add("TLS_DHE_DSS_WITH_AES_128_CBC_SHA");
        java8Ciphers.add("TLS_ECDHE_ECDSA_WITH_RC4_128_SHA");
        java8Ciphers.add("TLS_ECDHE_RSA_WITH_RC4_128_SHA");
        java8Ciphers.add("SSL_RSA_WITH_RC4_128_SHA");
        java8Ciphers.add("TLS_ECDH_ECDSA_WITH_RC4_128_SHA");
        java8Ciphers.add("TLS_ECDH_RSA_WITH_RC4_128_SHA");
        java8Ciphers.add("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384");
        java8Ciphers.add("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256");
        java8Ciphers.add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
        java8Ciphers.add("TLS_RSA_WITH_AES_256_GCM_SHA384");
        java8Ciphers.add("TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384");
        java8Ciphers.add("TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384");
        java8Ciphers.add("TLS_DHE_RSA_WITH_AES_256_GCM_SHA384");
        java8Ciphers.add("TLS_DHE_DSS_WITH_AES_256_GCM_SHA384");
        java8Ciphers.add("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        java8Ciphers.add("TLS_RSA_WITH_AES_128_GCM_SHA256");
        java8Ciphers.add("TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256");
        java8Ciphers.add("TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256");
        java8Ciphers.add("TLS_DHE_RSA_WITH_AES_128_GCM_SHA256");
        java8Ciphers.add("TLS_DHE_DSS_WITH_AES_128_GCM_SHA256");
        java8Ciphers.add("TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA");
        java8Ciphers.add("TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA");
        java8Ciphers.add("SSL_RSA_WITH_3DES_EDE_CBC_SHA");
        java8Ciphers.add("TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA");
        java8Ciphers.add("TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA");
        java8Ciphers.add("SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA");
        java8Ciphers.add("SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA");
        java8Ciphers.add("SSL_RSA_WITH_RC4_128_MD5");
        java8Ciphers.add("TLS_EMPTY_RENEGOTIATION_INFO_SCSV");

    }
}
