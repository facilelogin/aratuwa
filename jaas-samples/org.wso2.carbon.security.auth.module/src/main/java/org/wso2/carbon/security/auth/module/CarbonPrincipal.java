package org.wso2.carbon.security.auth.module;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.identity.model.ClaimValue;
import org.wso2.carbon.identity.model.Group;
import org.wso2.carbon.identity.model.Role;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * <code>CarbonLoginModule</code> will produce a <code>CarbonPrincipal</code> after successful authentication. the
 * downstream applications can use an instance of <code>CarbonPrincipal</code> retrieved through the
 * <code>Subject</code> to find user attributes groups and roles.
 *
 */
public class CarbonPrincipal implements Principal {

    protected String username;
    protected UserRealm realm;

    /**
     * 
     * @param username
     * @param userStoreManager
     */
    public CarbonPrincipal(String username, UserRealm realm) {
        super();
        this.username = username;
        this.realm = realm;
    }

    @Override
    public String getName() {
        return username;
    }

    /**
     * 
     * @return
     * @throws CarbonSecurityException
     */
    public List<Role> getRoles() throws CarbonSecurityException {

        String[] userRoles;
        List<Role> roleList = new ArrayList<Role>();

        try {
            userRoles = realm.getUserStoreManager()
                    .getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(username));
            if (userRoles != null && userRoles.length > 0) {
                for (String roleName : userRoles) {
                    roleList.add(new Role(roleName));
                }
            }
            return roleList;
        } catch (UserStoreException e) {
            throw new CarbonSecurityException(e);
        }
    }

    /**
     * 
     * @return
     * @throws CarbonSecurityException
     */
    public List<Group> getGroups() throws CarbonSecurityException {
        // TODO
        return null;
    }

    /**
     * 
     * @param attrNames
     * @return
     * @throws CarbonSecurityException
     */
    public List<ClaimValue> getAttributes(List<String> attrNames) throws CarbonSecurityException {

        String profileConfg = null;
        Map<String, String> userClaims = null;

        try {
            List<ClaimValue> claimList = new ArrayList<ClaimValue>();

            if (attrNames == null || attrNames.isEmpty()) {
                // no need to proceed further.
                return claimList;
            }

            userClaims = realm.getUserStoreManager().getUserClaimValues(
                    MultitenantUtils.getTenantAwareUsername(username), attrNames.toArray(new String[attrNames.size()]),
                    profileConfg);

            if (userClaims != null && !userClaims.isEmpty()) {
                for (Map.Entry<String, String> entry : userClaims.entrySet()) {
                    // there can be multi-valued attributes.
                    List<String> attrs = new ArrayList<String>();
                    attrs.add(entry.getValue());
                    claimList.add(new ClaimValue(entry.getKey(), attrs));
                }
            }
            return claimList;
        } catch (UserStoreException e) {
            throw new CarbonSecurityException(e);
        }
    }

    /**
     * this method is not visible to downstream applications. only visible to the <code>CarbonPermission</code> class or to
     * anyone who extends this class.
     * 
     * @param permission
     * @return
     * @throws CarbonSecurityException
     */
    protected boolean isAuthorized(CarbonPermission permission) throws CarbonSecurityException {
        try {

            if (permission == null) {
                throw new IllegalArgumentException("Permission object cannot be null");
            }

            return realm.getAuthorizationManager().isUserAuthorized(MultitenantUtils.getTenantAwareUsername(username),
                    permission.getName(), permission.getActions());
        } catch (UserStoreException e) {
            throw new CarbonSecurityException(e);
        }
    }

}