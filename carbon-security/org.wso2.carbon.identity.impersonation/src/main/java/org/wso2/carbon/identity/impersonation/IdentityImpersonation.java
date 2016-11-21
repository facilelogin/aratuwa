package org.wso2.carbon.identity.impersonation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authenticator.basicauth.BasicAuthenticator;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.impersonation.internal.IdentityImpersonationServiceComponent;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * this connector must only be present in an authentication step, where the user
 * is already identified by a previous step.
 *
 */
public class IdentityImpersonation extends BasicAuthenticator {

	private static final long serialVersionUID = -5855949224186253377L;
	private static Log log = LogFactory.getLog(IdentityImpersonation.class);
	private static final String AUTHENTICATOR_NAME = "identity-impersonation";
	private static final String IMPERSONATED_USERNAME_PARAMETER = "user";
	private static final String IMPERSONATION_ROLE = "Internal/Impersonation";
	private static final String IMPERSONATING_USER_CLAIM_URI = "http://wso2.org/claims/impersonating_user";

	@Override
	protected void processAuthenticationResponse(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, AuthenticationContext context)
			throws AuthenticationFailedException {

		try {
			String username = httpServletRequest.getParameter(IMPERSONATED_USERNAME_PARAMETER);
			AuthenticatedUser authnUser = getFirstStepAuthenticatedUser(context);

			UserRealm realm = getUserRealm(authnUser.getAuthenticatedSubjectIdentifier());
			String[] roleList = realm.getUserStoreManager().getRoleListOfUser(
					MultitenantUtils.getTenantAwareUsername(authnUser.getAuthenticatedSubjectIdentifier()));

			if (roleList != null && roleList.length > 0) {
				for (String role : roleList) {
					if (IMPERSONATION_ROLE.equalsIgnoreCase(role)) {
						context.setSubject(
								AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(username));
						authnUser.getUserAttributes().put(getClaimMapping(IMPERSONATING_USER_CLAIM_URI),
								authnUser.getAuthenticatedSubjectIdentifier());
						break;
					}
				}
			}

			context.setSubject(authnUser);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Override
	public boolean canHandle(HttpServletRequest httpServletRequest) {
		return httpServletRequest.getParameter(IMPERSONATED_USERNAME_PARAMETER) != null;
	}

	@Override
	public String getContextIdentifier(HttpServletRequest httpServletRequest) {
		return null;
	}

	@Override
	public String getName() {
		return AUTHENTICATOR_NAME;
	}

	@Override
	public String getFriendlyName() {
		return "identity-impersonation";
	}

	/**
	 * 
	 * @param context
	 * @return
	 * @throws IdentityProviderManagementException
	 */
	private AuthenticatedUser getFirstStepAuthenticatedUser(AuthenticationContext context)
			throws IdentityProviderManagementException {
		StepConfig firstStep = context.getSequenceConfig().getStepMap().get(1);
		return firstStep.getAuthenticatedUser();
	}

	/**
	 * 
	 * @param username
	 * @return
	 * @throws UserStoreException
	 */
	private org.wso2.carbon.user.core.UserRealm getUserRealm(String username) throws UserStoreException {
		org.wso2.carbon.user.core.UserRealm userRealm;
		String tenantDomain = MultitenantUtils.getTenantDomain(username);
		int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
		RealmService realmService = IdentityImpersonationServiceComponent.getRealmService();
		userRealm = (org.wso2.carbon.user.core.UserRealm) realmService.getTenantUserRealm(tenantId);
		return userRealm;
	}

	/**
	 * 
	 * @param uri
	 * @return
	 */
	private ClaimMapping getClaimMapping(String uri) {
		Claim localClaim = new Claim();
		localClaim.setClaimUri(uri);

		Claim remoteClaim = new Claim();
		remoteClaim.setClaimUri(uri);

		ClaimMapping mapping = new ClaimMapping();
		mapping.setLocalClaim(localClaim);
		mapping.setRemoteClaim(remoteClaim);

		return mapping;
	}

}
