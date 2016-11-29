package org.wso2.carbon.identity.impersonation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * this connector must only be present in an authentication step, where the user is already identified by the first
 * step.
 *
 */
public class IdentityImpersonation extends AbstractApplicationAuthenticator
        implements FederatedApplicationAuthenticator {

    private static final long serialVersionUID = -5855949224186253377L;
    private static Log log = LogFactory.getLog(IdentityImpersonation.class);
    private static final String AUTHENTICATOR_NAME = "identity-impersonation";
    private static final String IMPERSONATED_USERNAME_PARAMETER = "impersonated-user";
    private static final String INPUT_PAGE = "INPUT_PAGE";
    private static final String DEFAULT_INPUT_PAGE = "/authenticationendpoint/imp-user.jsp";
    private static final String IMPERSONATION_ADMIN_ROLE = "IMPERSONATION_ADMIN_ROLE";
    private static final String IMPERSONATION_USER_ROLE_LIST = "IMPERSONATION_USER_ROLE_LIST";
    private static final String SKIP_IMPERSONATION = "skip";
    private static final String DEFAULT_IMPERSONATION_ADMIN_ROLE = "Internal/impadmin";
    private static final String IMPERSONATION_ADMIN_CLAIM_URI = "IMPERSONATION_ADMIN_CLAIM_URI";
    private static final String DEFAULT_IMPERSONATION_ADMIN_CLAIM_URI = "http://wso2.org/claims/impersonation_admin";

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws AuthenticationFailedException, LogoutFailedException {

        try {
            // get the name of the user who needs to be impersonated from the request.
            String username = request.getParameter(IMPERSONATED_USERNAME_PARAMETER);
            UserRealm realm = null;
            boolean userExists = false;

            // loads the configuration. the configuration is loaded from the
            // fileIS_HOME/repository/conf/identity/application-authentication.xml.
            Map<String, String> configParams = getAuthenticatorConfig().getParameterMap();

            // true, if this request is generated from the INPUT_PAGE and the username is null.
            boolean skip = request.getParameter(SKIP_IMPERSONATION) != null && (username == null || username.isEmpty());

            // the impersonating admin username will be included in the below claim uri - and passed back to the
            // application.
            String impAdminUserClamUri = configParams.get(IMPERSONATION_ADMIN_CLAIM_URI);
            if (impAdminUserClamUri == null || impAdminUserClamUri.isEmpty()) {
                impAdminUserClamUri = DEFAULT_IMPERSONATION_ADMIN_CLAIM_URI;
            }

            // find the role the admin user should have to impersonate other users.
            String impAdminRole = configParams.get(IMPERSONATION_ADMIN_ROLE);
            if (impAdminRole == null || impAdminRole.isEmpty()) {
                impAdminRole = DEFAULT_IMPERSONATION_ADMIN_ROLE;
            }

            // this assumes the user is authenticated from the first step.
            AuthenticatedUser impAdminUser = getFirstStepAuthenticatedUser(context);
            AuthenticatedUser impUser = null;

            // check whether the admin user has the required role.
            realm = getUserRealm(impAdminUser.getAuthenticatedSubjectIdentifier());
            // get all the roles of the admin user.
            String[] adminRoleList = realm.getUserStoreManager().getRoleListOfUser(
                    MultitenantUtils.getTenantAwareUsername(impAdminUser.getAuthenticatedSubjectIdentifier()));

            if (username == null || username.isEmpty()) {
                boolean hasAdminRole = false;
                for (String role : adminRoleList) {
                    if (impAdminRole.equalsIgnoreCase(role)) {
                        // if the admin user is not in the required role, we do not want to continue any more.
                        hasAdminRole = true;
                        break;
                    }
                }

                if (!hasAdminRole) {
                    skip = true;
                }
            }

            if (!skip && impAdminUser != null) {
                // first time - or with no username.
                // check whether the impersonated user exists in the system.
                if (username != null && !username.isEmpty()) {
                    realm = getUserRealm(username);
                    userExists = realm.getUserStoreManager().isExistingUser(username);
                }

                if (!userExists) {
                    // if the impersonated user is not in the system, then we need to return back an error.
                    String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(context.getQueryParams(),
                            context.getCallerSessionKey(), context.getContextIdentifier());

                    String loginPage = configParams.get(INPUT_PAGE);
                    if (loginPage == null || loginPage.isEmpty()) {
                        loginPage = DEFAULT_INPUT_PAGE;
                    }

                    String retryParam = "";
                    if (username != null) {
                        // user does not exist in the system. we need a valid username.
                        retryParam = "&impFailure=true&impFailureMsg=imp.no.user";
                    }
                    try {
                        response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams))
                                + "&authenticators=" + getName() + retryParam);
                        return AuthenticatorFlowStatus.INCOMPLETE;
                    } catch (IOException e) {
                        throw new AuthenticationFailedException("Authentication failed!", e);
                    }
                }

                // find the role the impersonated user should have to be impersonated by other admins.
                String impUserRoles = configParams.get(IMPERSONATION_USER_ROLE_LIST);
                List<String> impUserRoleList = new ArrayList<String>();
                if (impUserRoles != null && !impUserRoles.isEmpty()) {
                    String[] roleArr = impUserRoles.split(",");
                    for (String role : roleArr) {
                        impUserRoleList.add(role.toLowerCase());
                    }
                }

                if (adminRoleList != null && adminRoleList.length > 0) {
                    for (String role : adminRoleList) {
                        if (impAdminRole.equalsIgnoreCase(role)) {
                            // we have the admin user in the required role!
                            boolean validImpersonatedUser = false;
                            if (impUserRoleList.isEmpty()) {
                                // no restrictions on the user list - so it can be any user.
                                validImpersonatedUser = true;
                            }

                            if (!validImpersonatedUser) {
                                // get all the roles of the impersonated user.
                                String[] userRoleList = realm.getUserStoreManager()
                                        .getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(username));

                                if (userRoleList != null && userRoleList.length > 0) {
                                    for (String improle : userRoleList) {
                                        if (impUserRoleList.contains(improle.toLowerCase())) {
                                            validImpersonatedUser = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (validImpersonatedUser) {
                                impUser = AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(username);
                                impUser.getUserAttributes().put(getClaimMapping(impAdminUserClamUri),
                                        impAdminUser.getAuthenticatedSubjectIdentifier());
                                context.setSubject(populateRequestedAttributes(context, impUser, impAdminUserClamUri));
                            }

                            break;
                        }
                    }
                }

                if (impUser == null) {
                    // this means the impersonating user does not have the required role to impersonate.
                    String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(context.getQueryParams(),
                            context.getCallerSessionKey(), context.getContextIdentifier());
                    String loginPage = configParams.get(INPUT_PAGE);
                    if (loginPage == null || loginPage.isEmpty()) {
                        loginPage = DEFAULT_INPUT_PAGE;
                    }

                    String retryParam = "";
                    if (username != null) {
                        retryParam = "&impFailure=true&impFailureMsg=imp.not.authz";
                    }
                    try {
                        response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams))
                                + "&authenticators=" + getName() + retryParam);
                        return AuthenticatorFlowStatus.INCOMPLETE;
                    } catch (IOException e) {
                        throw new AuthenticationFailedException("Authentication failed!", e);
                    }
                }
            }

            if (impUser == null) {
                // this is when the impersonation is skipped by the user or this handler is engaged in the first step,
                // which is wrong.
                if (impAdminUser == null) {
                    // this handler is engaged in the first step, which is wrong.
                    log.error("This handler is engaged in the first step, which is wrong");
                    return AuthenticatorFlowStatus.FAIL_COMPLETED;
                } else {
                    context.setSubject(impAdminUser);
                }
            } else {
                log.info(String.format("The user %s is impersonated by the user %s",
                        impUser.getAuthenticatedSubjectIdentifier(), impAdminUser.getAuthenticatedSubjectIdentifier()));
            }

            if (log.isDebugEnabled()) {
                Map<ClaimMapping, String> claimMap = context.getSubject().getUserAttributes();
                Iterator<Entry<ClaimMapping, String>> it = claimMap.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<ClaimMapping, String> pair = it.next();
                    log.debug(String.format("User attribute key: %s. Value: %s", pair.getKey(), pair.getValue()));
                }
            }
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        } catch (Exception e) {
            log.error(e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }

    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {
        // we assume this is the only authenticator in this step.
        return true;
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
        return AUTHENTICATOR_NAME;
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
        RealmService realmService = IdentityTenantUtil.getRealmService();
        userRealm = (org.wso2.carbon.user.core.UserRealm) realmService.getTenantUserRealm(tenantId);
        return userRealm;
    }

    /**
     * 
     * @param context
     * @param imperonsatedUser
     * @param impersonatingUserClaimUri
     * @throws org.wso2.carbon.user.core.UserStoreException
     * @throws UserStoreException
     */
    private AuthenticatedUser populateRequestedAttributes(AuthenticationContext context,
            AuthenticatedUser imperonsatedUser, String impersonatingUserClaimUri)
            throws org.wso2.carbon.user.core.UserStoreException, UserStoreException {

        Map<String, String> requestedClaims = context.getSequenceConfig().getApplicationConfig()
                .getRequestedClaimMappings();

        if (requestedClaims == null || requestedClaims.isEmpty()) {
            // no requested attributes - don't worry.
            if (log.isDebugEnabled()) {
                log.debug(String.format("No requested claim configured for the service porvider %s.",
                        context.getSequenceConfig().getApplicationConfig().getApplicationName()));
            }
            return imperonsatedUser;
        }

        Iterator<Map.Entry<String, String>> it = requestedClaims.entrySet().iterator();
        List<String> filteredReqClaimList = new ArrayList<String>();

        // we should not be adding the impersonating user claim uri, which we have already added. at the same time we
        // need to build a list of attributes to send to the user api.
        while (it.hasNext()) {
            Map.Entry<String, String> pair = it.next();
            if (!impersonatingUserClaimUri.equalsIgnoreCase(pair.getKey())) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Filtered requested claims for the service porvider %s. Claim uri: %s",
                            context.getSequenceConfig().getApplicationConfig().getApplicationName(), pair.getKey()));
                }
                filteredReqClaimList.add(pair.getKey());
            }
        }

        UserStoreManager userStoreManager = getUserRealm(imperonsatedUser.getAuthenticatedSubjectIdentifier())
                .getUserStoreManager();
        String tenantAwareUsername = MultitenantUtils
                .getTenantAwareUsername(imperonsatedUser.getAuthenticatedSubjectIdentifier());
        // retrieve user attributes from the underlying user store.
        Map<String, String> requestedClaimValues = userStoreManager.getUserClaimValues(tenantAwareUsername,
                filteredReqClaimList.toArray(new String[filteredReqClaimList.size()]), null);

        if (requestedClaimValues == null || requestedClaimValues.isEmpty()) {
            // user does not have any values for the requested claims.
            if (log.isDebugEnabled()) {
                log.debug(String.format("No requested claim values found for the service porvider %s for the user %s",
                        context.getSequenceConfig().getApplicationConfig().getApplicationName(), tenantAwareUsername));
            }
            return imperonsatedUser;
        }

        Iterator<Map.Entry<String, String>> itvalues = requestedClaimValues.entrySet().iterator();
        while (itvalues.hasNext()) {
            Map.Entry<String, String> pair = itvalues.next();
            imperonsatedUser.getUserAttributes().put(getClaimMapping(pair.getKey()), pair.getValue());
            if (log.isDebugEnabled()) {
                log.debug(
                        String.format("Requested claim values added to the authenticated user %s. Key: %s. Value: %s.",
                                tenantAwareUsername, pair.getKey(), pair.getValue()));
            }
        }

        return imperonsatedUser;
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

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws AuthenticationFailedException {

    }
}