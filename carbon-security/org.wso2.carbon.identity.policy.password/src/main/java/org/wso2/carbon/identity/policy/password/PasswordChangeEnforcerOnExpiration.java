package org.wso2.carbon.identity.policy.password;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

/**
 * this connector must only be present in an authentication step, where the user
 * is already identified by a previous step.
 *
 */
public class PasswordChangeEnforcerOnExpiration extends AbstractApplicationAuthenticator
		implements LocalApplicationAuthenticator {

	private static final String AUTH_NAME = "password-reset-enforcer";
	private static final String AUTHENTICATOR_TYPE = "LOCAL";
	private static final String STATE = "state";
	private static final String CURRENT_PWD = "CURRENT_PWD";
	private static final String NEW_PWD = "NEW_PWD";
	private static final String NEW_PWD_CONFIRMATION = "NEW_PWD_CONFIRMATION";
	public static final String LAST_PASSWORD_CHANGED_TIMESTAMP_CLAIM = "http://wso2.org/claims/lastPasswordChangedTimestamp";

	private static final Log log = LogFactory.getLog(PasswordChangeEnforcerOnExpiration.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 307784186695787941L;

	/**
	 * if this handler is engaged in a step - only this must be present - no
	 * other connectors.
	 */
	@Override
	public boolean canHandle(HttpServletRequest arg0) {
		return true;
	}

	@Override
	public String getContextIdentifier(HttpServletRequest request) {
		return request.getParameter(STATE);
	}

	@Override
	public String getFriendlyName() {
		return AUTH_NAME;

	}

	@Override
	public String getName() {
		return AUTH_NAME;
	}

	@Override
	public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context) throws AuthenticationFailedException, LogoutFailedException {

		// if the logout request comes, then no need to go through and doing
		// complete the flow.
		if (context.isLogoutRequest()) {
			return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
		}

		if (StringUtils.isNotEmpty(request.getParameter(CURRENT_PWD))
				&& StringUtils.isNotEmpty(request.getParameter(NEW_PWD))
				&& StringUtils.isNotEmpty(request.getParameter(NEW_PWD_CONFIRMATION))) {
			try {
				processAuthenticationResponse(request, response, context);
			} catch (Exception e) {
				context.setRetrying(true);
				context.setCurrentAuthenticator(getName());
				return initiateAuthRequest(request, response, context, e.getMessage());
			}
			return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
		} else {
			return initiateAuthRequest(request, response, context, null);
		}

	}

	/**
	 * this will prompt user to change the credentials only if the last password
	 * changed time has gone beyond the pre-configured value.
	 * 
	 * @param request
	 * @param response
	 * @param context
	 * @return
	 * @throws AuthenticationFailedException
	 */
	protected AuthenticatorFlowStatus initiateAuthRequest(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context, String errorMessage) throws AuthenticationFailedException {

		// find the authenticated user.
		AuthenticatedUser authenticatedUser = getUsername(context);

		if (authenticatedUser == null) {
			// redirect to an error page. cannot proceed further without
			// identifying the user.
		}

		String username = null;
		String tenantDomain = null;
		String userstoreDomain = null;
		String tenantAwareUsername = null;
		String fullyQualifiedUsername = null;

		username = authenticatedUser.getAuthenticatedSubjectIdentifier();
		tenantDomain = authenticatedUser.getTenantDomain();
		userstoreDomain = authenticatedUser.getUserStoreDomain();
		tenantAwareUsername = UserCoreUtil.addDomainToName(username, userstoreDomain);
		fullyQualifiedUsername = UserCoreUtil.addTenantDomainToEntry(tenantAwareUsername, tenantDomain);

		int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

		RealmService realmService = IdentityTenantUtil.getRealmService();
		UserRealm userRealm;
		UserStoreManager userStoreManager;

		try {
			userRealm = realmService.getTenantUserRealm(tenantId);
			userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
		} catch (UserStoreException e) {
			// this exception will be handled by the framework.
			throw new AuthenticationFailedException("Error occured while loading userrealm/userstoremanager", e);
		}

		long currentTimeMillis = System.currentTimeMillis();
		String passwordLastChangedTime = null;

		try {
			passwordLastChangedTime = userStoreManager.getUserClaimValue(tenantAwareUsername,
					LAST_PASSWORD_CHANGED_TIMESTAMP_CLAIM, null);
		} catch (org.wso2.carbon.user.core.UserStoreException e) {
			// this exception will be handled by the framework.
			throw new AuthenticationFailedException(
					"Error occured while loading user claim - http://wso2.org/claims/lastPasswordChangedTimestamp", e);
		}

		long passwordChangedTime = 0;
		int daysDifference = 0;

		if (passwordLastChangedTime != null) {
			passwordChangedTime = Long.parseLong(passwordLastChangedTime);
		}

		if (passwordChangedTime > 0) {
			Calendar currentTime = Calendar.getInstance();
			currentTime.add(Calendar.DATE, (int) currentTime.getTimeInMillis());
			daysDifference = (int) ((currentTimeMillis - passwordChangedTime) / (1000 * 60 * 60 * 24));
		}

		if (daysDifference > Utils.getPasswordExpirationInDays() || passwordLastChangedTime == null) {
			// the password has changed or the password changed time is not set.

			String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL().replace("login.do",
					"pwd-reset.jsp");
			String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(context.getQueryParams(),
					context.getCallerSessionKey(), context.getContextIdentifier());

			try {
				String retryParam = "";

				if (context.isRetrying()) {
					retryParam = "&authFailure=true&authFailureMsg=" + errorMessage;
				}

				response.sendRedirect(response
						.encodeRedirectURL(loginPage + ("?" + queryParams + "&username=" + fullyQualifiedUsername))
						+ "&authenticators=" + getName() + ":" + AUTHENTICATOR_TYPE + retryParam);
			} catch (IOException e) {
				throw new AuthenticationFailedException(e.getMessage(), e);
			}

			context.setCurrentAuthenticator(getName());
			return AuthenticatorFlowStatus.INCOMPLETE;
		}

		// authentication is now completed in this step. update the
		// authenticated user information.
		updateAuthenticatedUserInStepConfig(context, authenticatedUser);
		return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
	}

	/**
	 * 
	 */
	@Override
	protected void processAuthenticationResponse(HttpServletRequest req, HttpServletResponse resp,
			AuthenticationContext context) throws AuthenticationFailedException {

		String username = null;
		String tenantDomain = null;
		String userstoreDomain = null;
		String tenantAwareUsername = null;

		AuthenticatedUser authenticatedUser = getUsername(context);
		username = authenticatedUser.getAuthenticatedSubjectIdentifier();
		tenantDomain = authenticatedUser.getTenantDomain();
		userstoreDomain = authenticatedUser.getUserStoreDomain();

		tenantAwareUsername = UserCoreUtil.addDomainToName(username, userstoreDomain);

		int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
		RealmService realmService = IdentityTenantUtil.getRealmService();
		UserRealm userRealm;
		UserStoreManager userStoreManager;

		try {
			userRealm = realmService.getTenantUserRealm(tenantId);
			userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
		} catch (UserStoreException e) {
			throw new AuthenticationFailedException("Error occured while loading userrealm/userstoremanager", e);
		}

		String currentPassword = req.getParameter(CURRENT_PWD);
		String newPassword = req.getParameter(NEW_PWD);
		String repeatPassword = req.getParameter(NEW_PWD_CONFIRMATION);

		if (newPassword != null && newPassword.equals(repeatPassword)) {

			try {
				userStoreManager.updateCredential(tenantAwareUsername, newPassword, currentPassword);
				log.info("Updated user credentials of " + tenantAwareUsername);
			} catch (org.wso2.carbon.user.core.UserStoreException e) {
				throw new AuthenticationFailedException("Incorrect current password", e);
			}

			// authentication is now completed in this step. update the
			// authenticated user information.
			updateAuthenticatedUserInStepConfig(context, authenticatedUser);

		} else {
			throw new AuthenticationFailedException("New password does not match with the new passsword confirmation");
		}

	}

	/**
	 * 
	 * @param context
	 * @return
	 * @throws AuthenticationFailedException
	 */
	private AuthenticatedUser getUsername(AuthenticationContext context) {
		// username from authentication context.
		AuthenticatedUser authenticatedUser = null;
		for (int i = 1; i <= context.getSequenceConfig().getStepMap().size(); i++) {
			StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(i);
			if (stepConfig.getAuthenticatedUser() != null && stepConfig.getAuthenticatedAutenticator()
					.getApplicationAuthenticator() instanceof LocalApplicationAuthenticator) {
				authenticatedUser = stepConfig.getAuthenticatedUser();

				if (log.isDebugEnabled()) {
					log.debug("username :" + authenticatedUser.toString());
				}
				break;
			}
		}

		return authenticatedUser;
	}

	/**
	 * 
	 * @param context
	 * @param username
	 */
	private void updateAuthenticatedUserInStepConfig(AuthenticationContext context,
			AuthenticatedUser authenticatedUser) {
		for (int i = 1; i <= context.getSequenceConfig().getStepMap().size(); i++) {
			StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(i);
			stepConfig.setAuthenticatedUser(authenticatedUser);
		}

		context.setSubject(authenticatedUser);
	}

}
