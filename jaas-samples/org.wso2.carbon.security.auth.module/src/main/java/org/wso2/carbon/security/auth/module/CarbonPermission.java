package org.wso2.carbon.security.auth.module;

import java.security.AccessController;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.Principal;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * the applications, which are deployed on Carbon should use CarbonPermission to authorize users.
 * 
 * <code>
      AccessController.checkPermission(new CarbonPermission("/r1/r2/r3","a1"));
 *</code>
 */
public class CarbonPermission extends BasicPermission {

    /**
     * 
     */
    private static final long serialVersionUID = -1657785586716443340L;
    private final static Log log = LogFactory.getLog(CarbonPermission.class);

    /**
     * 
     * @param permission
     */
    public CarbonPermission(String resource, String action) {
        super(resource, action);

        if (action == null || action.isEmpty()) {
            throw new IllegalArgumentException("Permission action cannot be null");
        }

        if (resource == null || resource.isEmpty()) {
            throw new IllegalArgumentException("Permission name cannot be null");
        }

        if (log.isDebugEnabled()) {
            log.debug("A CarbonPermission object is created for resource: " + resource + " and action: " + action);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean implies(Permission permission) {

        // we do not need to worry about - if it is not a CarbonPermission
        if (!(permission instanceof CarbonPermission)) {
            return super.implies(permission);
        }

        CarbonPrincipal carbonPrincipal = null;
        CarbonPermission carbonPermission = (CarbonPermission) permission;

        // get the current subject.
        Subject subject = Subject.getSubject(AccessController.getContext());

        // find the CarbonPrincipal
        for (Principal principal : subject.getPrincipals()) {
            if (principal instanceof CarbonPrincipal) {
                carbonPrincipal = (CarbonPrincipal) principal;
                // we only need CarbonPrincipal. break now.
                break;
            }
        }

        if (carbonPrincipal == null) {
            // no CarbonPrincipal found. we do not want to handle.
            return super.implies(permission);
        } else {
            try {

                if (log.isDebugEnabled()) {
                    log.debug("Evaluating permissions for resource: " + carbonPermission.getName() + " and action: "
                            + carbonPermission.getActions() + " and subject: " + carbonPrincipal.getName());
                }
                // here we have CarbonPrincipal.
                return carbonPrincipal.isAuthorized(carbonPermission);
            } catch (CarbonSecurityException e) {
                log.error(e);
                return false;
            }
        }
    }
}