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

package org.wso2.carbon.identity.impersonation.internal;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.impersonation.IdentityImpersonation;

/**
 * @scr.component name="org.wso2.carbon.identity.impersonation.component" immediate="true"
 */
public class IdentityImpersonationServiceComponent {

    private static Log log = LogFactory.getLog(IdentityImpersonationServiceComponent.class);

    /**
     * 
     * @param ctxt
     */
    protected void activate(ComponentContext ctxt) {
        try {

            // register this extension facilitate impersonation during the login flow.
            IdentityImpersonation authenticator = new IdentityImpersonation();
            Hashtable<String, String> props = new Hashtable<String, String>();
            ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(), authenticator, props);

            if (log.isDebugEnabled()) {
                log.debug("org.wso2.carbon.identity.impersonation.component is activated");
            }
        } catch (Throwable e) {
            log.fatal("Error while activating the org.wso2.carbon.identity.impersonation.component", e);
        }
    }

    /**
     * 
     * @param ctxt
     */
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("org.wso2.carbon.identity.impersonation.component is deactivated");
        }
    }
}
