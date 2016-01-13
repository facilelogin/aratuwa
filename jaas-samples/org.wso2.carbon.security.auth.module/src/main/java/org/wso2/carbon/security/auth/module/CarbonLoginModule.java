package org.wso2.carbon.security.auth.module;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class CarbonLoginModule implements LoginModule {

	private final static Log log = LogFactory.getLog(CarbonLoginModule.class);
	private CallbackHandler callbackHandler;
	private Subject subject;

	/**
	 * 
	 */
	private static final ResourceBundle rb = AccessController.doPrivileged(new PrivilegedAction<ResourceBundle>() {
		public ResourceBundle run() {
			return ResourceBundle.getBundle("sun.security.util.AuthResources");
		}
	});

	/**
	 * 
	 */
	public boolean abort() throws LoginException {
		return false;
	}

	/**
	 * 
	 */
	public boolean commit() throws LoginException {
		return true;
	}

	/**
	 * 
	 */
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
			Map<String, ?> options) {
		this.subject = subject;
	}

	/**
	 * 
	 */
	public boolean login() throws LoginException {

		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback(rb.getString("username: "));
		callbacks[1] = new PasswordCallback(rb.getString("password: "), false);

		String username;
		char[] password;

		try {
			callbackHandler.handle(callbacks);
			username = ((NameCallback) callbacks[0]).getName();
			char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
			password = new char[tmpPassword.length];
			System.arraycopy(tmpPassword, 0, password, 0, tmpPassword.length);
			((PasswordCallback) callbacks[1]).clearPassword();

		} catch (java.io.IOException ioe) {
			throw new LoginException(ioe.toString());

		} catch (UnsupportedCallbackException uce) {
			throw new LoginException("Error: " + uce.getCallback().toString()
					+ " not available to acquire authentication information" + " from the user");
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
				log.debug("Login request from: Usename: " + tenantAwareUsername + " Tenant Domain: " + tenantDomain);
			}

			userRealm = realmService.getTenantUserRealm(tenantId);
			isAuthenticated = userRealm.getUserStoreManager().authenticate(tenantAwareUsername, new String(password));

			if (isAuthenticated) {
				Set<Principal> principals = subject.getPrincipals();
				principals.add(new CarbonPrincipal(username, userRealm));
			}

		} catch (Exception e) {
			if (isAuthenticated) {
				log.error("Login passed but failed due to an internal error", e);
			} else {
				log.error("Login failure due to an internal error", e);
			}
		}

		return true;
	}

	public boolean logout() throws LoginException {
		return true;
	}

}