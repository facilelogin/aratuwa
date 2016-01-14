package org.wso2.carbon.security.auth.module;

import java.io.IOException;
import java.security.Principal;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * when <code>CarbonLoginModule</code> is configured in the jaas.conf to be the login module,this will take care of
 * authenticating user over the underlying user store. following shows a sample jaas.conf.
 * 
 * <code>
   CarbonLogin {
         org.wso2.java.security.is.CarbonLoginModule required
   };
 </code>
 *
 * following code shows how to login with this module.
 * 
 * <code>
   LoginContext context = new LoginContext("CarbonLogin", new CallbackHandler() {
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        ((NameCallback) callback).setName("admin");
                    } else if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword("admin".toCharArray());
                    }
                }

         }
   });

   context.login();
   Subject subject = context.getSubject();
 
 *</code>
 */
public class CarbonLoginModule implements LoginModule {

    private final static Log log = LogFactory.getLog(CarbonLoginModule.class);
    private CallbackHandler callbackHandler;
    private Principal principal = null;
    private Subject subject;
    private boolean committed = false;

    /**
     * the initialize method is called to initialize the LoginModule with the relevant authentication and state
     * information. this method is called by a LoginContext immediately after this LoginModule has been instantiated,
     * and prior to any calls to its other public methods. The method implementation should store away the provided
     * arguments for future use.
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    /**
     * the login method is called to authenticate a Subject. This is phase 1 of authentication. this method
     * implementation performs the actual authentication.
     */
    public boolean login() throws LoginException {

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);

        String username;
        char[] password;

        try {
            callbackHandler.handle(callbacks);
            username = ((NameCallback) callbacks[0]).getName();
            char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
            password = new char[tmpPassword.length];
        } catch (IOException e) {
            throw new LoginException(e.toString());
        } catch (UnsupportedCallbackException e) {
            throw new LoginException(e.toString());
        }

        RealmService realmService;
        UserRealm userRealm;
        boolean isAuthenticated = false;

        // get a handle to the RealmService OSGi service
        realmService = (RealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(RealmService.class, new Hashtable<String, String>());

        try {

            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(username);
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);

            if (log.isDebugEnabled()) {
                log.debug("Login request from: Username: " + tenantAwareUsername + " Tenant Domain: " + tenantDomain);
            }

            userRealm = realmService.getTenantUserRealm(tenantId);
            isAuthenticated = userRealm.getUserStoreManager().authenticate(tenantAwareUsername, new String(password));

            if (isAuthenticated) {
                principal = new CarbonPrincipal(username, userRealm);
            } else {
                throw new FailedLoginException("Invalid username or password.");
            }

        } catch (Exception e) {
            log.error(e);
            throw new FailedLoginException("Invalid username or password.");
        }

        return true;
    }

    /**
     * the <code>commit<code> method is called to commit the authentication process. This is phase 2 of authentication
     * when phase 1 succeeds. It is called if the LoginContext's overall authentication succeeded
     */
    public boolean commit() throws LoginException {
        if (principal != null) {
            Set<Principal> principals = subject.getPrincipals();
            principals.add(principal);
            committed = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * the abort method is called to abort the authentication process. this is phase 2 of authentication when phase 1
     * fails. it is called if the LoginContext's overall authentication failed.
     */
    public boolean abort() throws LoginException {
        // if our authentication was not successful, just return false
        if (principal == null) {
            return false;
        }

        // clean up if overall authentication failed
        if (committed)
            logout();
        else {
            committed = false;
            principal = null;
        }

        return true;
    }

    /**
     * this method removes Principals, and removes/destroys credentials associated with the Subject during the commit
     * operation. this method should not touch those Principals or credentials previously existing in the Subject, or
     * those added by other LoginModules.
     */
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(principal);
        committed = false;
        principal = null;
        return true;
    }

}