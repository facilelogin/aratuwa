package org.wso2.carbon.security.login;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * 
 * this class exposes login as a lightweight API. you need to deploy the login.war inside
 * repository/deployment/server/webapps directory of the WSO2 Identity Server. the login API can be invoked with
 * different parameters, asking for all the roles of the user who authenticates, or a set of user attributes.
 *
 */
@Path("/")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces(MediaType.APPLICATION_JSON)
public class LoginResource {

    private final static Log log = LogFactory.getLog(LoginResource.class);

    // keeps track of HttpServletRequest and HttpServletResponse
    @Context
    private MessageContext context;

    /**
     * the login API can be invoked with different parameters, asking for all the roles of the user who authenticates,
     * or a set of user attributes.
     * 
     * @param loginRequest carries the user credentials and other related attributes.
     * @return 200 OK for successful authentication with the requested claims and roles. 401 for unauthorized access.400
     *         for bad request
     */
    @POST
    public Response login(LoginRequest loginRequest) {

        RealmService realmService;
        UserRealm userRealm;
        boolean isAuthenticated = false;

        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()
                || loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{'error': 'Invalid inputs, either the username or the password is empty.'}").build();
        }

        // get a handle to the RealmService OSGi service
        realmService = (RealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(RealmService.class, new Hashtable<String, String>());

        try {

            String tenantDomain = MultitenantUtils.getTenantDomain(loginRequest.getUsername());
            String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(loginRequest.getUsername());
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);

            if (log.isDebugEnabled()) {
                log.debug("Login request from: Usename: " + tenantAwareUsername + " Tenant Domain: " + tenantDomain);
            }

            userRealm = realmService.getTenantUserRealm(tenantId);
            isAuthenticated = userRealm.getUserStoreManager().authenticate(tenantAwareUsername,
                    loginRequest.getPassword());

            if (isAuthenticated) {
                String[] userRoles = new String[] {};
                String profileConfg = null;
                Map<String, String> userClaims = null;

                if (loginRequest.isWithRoles()) {
                    userRoles = userRealm.getUserStoreManager().getRoleListOfUser(tenantAwareUsername);
                }

                if (loginRequest.getClaims() != null && loginRequest.getClaims().size() > 0) {
                    userClaims = userRealm.getUserStoreManager().getUserClaimValues(tenantAwareUsername,
                            loginRequest.getClaims().toArray(new String[loginRequest.getClaims().size()]),
                            profileConfg);
                }

                return Response.status(Response.Status.OK)
                        .entity(LoginResponse.build(loginRequest.getUsername(), Arrays.asList(userRoles), userClaims))
                        .build();
            }

            // authentication failed. just returning back a 401
            return Response.status(Response.Status.UNAUTHORIZED).build();

        } catch (Exception e) {
            if (isAuthenticated) {
                log.error("Login passed but failed due to an internal error", e);
            } else {
                log.error("Login failure due to an internal error", e);
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{'error':'" + e.getMessage() + "' }")
                    .build();
        }

    }
}