/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.security.tls;

import java.security.Provider;
import java.security.Security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

/**
 * @scr.component name="org.wso2.carbon.security.tls.component" immediate="true"
 */
public class CarbonTLSDump {

    private static Log log = LogFactory.getLog(CarbonTLSDump.class);
    private static final String JAVA_VERSION = Runtime.class.getPackage().getSpecificationVersion();

    /**
     * 
     * @param ctxt
     */
    protected void activate(ComponentContext context) {

        try {

            // returns an array containing all the installed providers. the order of the providers in the array is their
            // preference order.
            Provider providers[] = Security.getProviders();

            StringBuilder buffer = new StringBuilder();

            buffer.append(System.lineSeparator());
            buffer.append(System.lineSeparator());
            buffer.append("[The list of crypto providers available in the system]" + System.lineSeparator());
            buffer.append(System.lineSeparator());

            for (int i = 0; i < providers.length; i++) {
                buffer.append(
                        (providers[i].getName() + ":" + providers[i].getClass().getName() + System.lineSeparator()));
            }

            // returns the default SSL server socket factory.
            // the first time this method is called, the security property "ssl.ServerSocketFactory.provider" is
            // examined. if it is non-null, a class by that name is loaded and instantiated. if that is successful and
            // the object is an instance of SSLServerSocketFactory, it is made the default SSL server socket factory.
            // otherwise, this method returns SSLContext.getDefault().getServerSocketFactory(). if that call fails, an
            // inoperative factory is returned.
            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

            buffer.append(System.lineSeparator());

            buffer.append("[Java Secure Socket Extension (JSSE)]" + System.lineSeparator());
            buffer.append(System.lineSeparator());

            buffer.append(
                    "JSSE provider name: " + SSLContext.getDefault().getProvider().getName() + System.lineSeparator());
            buffer.append(
                    "JSSE provider info: " + SSLContext.getDefault().getProvider().getInfo() + System.lineSeparator());
            buffer.append("JSSE implementation class name: "
                    + SSLContext.getDefault().getProvider().getClass().getName() + System.lineSeparator());
            buffer.append(System.lineSeparator());

            // returns a copy of the SSLParameters indicating the default settings for this SSL context.
            // the parameters will always have the cipher suites and protocols arrays set to non-null values.
            SSLParameters sslParams = SSLContext.getDefault().getDefaultSSLParameters();

            buffer.append("[Configuration data from catalina-server.xml]" + System.lineSeparator());
            buffer.append(System.lineSeparator());

            buffer.append("Cipher suites configured in the system: " + System.lineSeparator());
            loadFromArray(sslParams.getCipherSuites(), buffer);
            buffer.append(System.lineSeparator());

            buffer.append("TLS/SSL protocols configured in the system: " + System.lineSeparator());
            loadFromArray(sslParams.getProtocols(), buffer);
            buffer.append(System.lineSeparator());

            buffer.append(
                    "Client authentication is required ? " + sslParams.getNeedClientAuth() + System.lineSeparator());
            buffer.append(
                    "Client authentication is optional? " + sslParams.getWantClientAuth() + System.lineSeparator());
            buffer.append(System.lineSeparator());

            buffer.append("[Runtime SSL/TLS details]" + System.lineSeparator());
            buffer.append(System.lineSeparator());

            // returns the names of the cipher suites which could be enabled for use on an SSL connection created by
            // this factory. normally, only a subset of these will actually be enabled by default, since this list may
            // include cipher suites which do not meet quality of service requirements for those defaults. such cipher
            // suites are useful in specialized applications.
            String[] availableCiphers = ssf.getSupportedCipherSuites();

            buffer.append("All available cipher suites from the JSSE provider in the system:" + System.lineSeparator());

            boolean isJdkPatched = false;

            for (int i = 0; i < availableCiphers.length; ++i) {

                if (JAVA_VERSION.equals("1.8")
                        && Java8CipherUtil.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384.equals(availableCiphers[i])) {
                    isJdkPatched = true;
                } else if (JAVA_VERSION.equals("1.7")
                        && Java7CipherUtil.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384.equals(availableCiphers[i])) {
                    isJdkPatched = true;
                }

                buffer.append("\t" + "\t" + availableCiphers[i] + System.lineSeparator());
            }

            buffer.append(System.lineSeparator());

            // returns the list of cipher suites which are enabled by default. unless a different list is enabled,
            // handshaking on an SSL connection will use one of these cipher suites. The minimum quality of service for
            // these defaults requires confidentiality protection and server authentication (that is, no anonymous
            // cipher suites).
            String[] defaultCiphers = ssf.getDefaultCipherSuites();

            buffer.append("The list of cipher suites functional in the system with the JSSE provider:"
                    + System.lineSeparator());

            for (int i = 0; i < defaultCiphers.length; ++i) {
                buffer.append("\t" + "\t" + defaultCiphers[i] + System.lineSeparator());
            }

            buffer.append(System.lineSeparator());

            buffer.append("Is the JDK patched with JCE unlimited strength jurisdiction policy files ? " + isJdkPatched
                    + System.lineSeparator());

            log.info(buffer.toString());

        } catch (Throwable e) {
            log.error(e);
        }

    }

    /**
     * 
     * @param arr
     * @param buffer
     */
    private void loadFromArray(String[] arr, StringBuilder buffer) {
        for (String elm : arr) {
            buffer.append("\t" + "\t" + elm + System.lineSeparator());
        }
    }

    /**
     * 
     * @param context
     */
    protected void deactivate(ComponentContext ctxt) {

    }
}
