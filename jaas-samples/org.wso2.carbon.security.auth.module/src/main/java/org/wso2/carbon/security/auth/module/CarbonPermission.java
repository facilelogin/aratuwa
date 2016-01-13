package org.wso2.carbon.security.auth.module;

import java.security.AccessController;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.Principal;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
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
	}

	/**
	 * 
	 */
	@Override
	public boolean implies(Permission permission) {

		if (!(permission instanceof CarbonPermission)) {
			return super.implies(permission);
		}

		CarbonPrincipal carbonPrincipal = null;
		Subject subject = Subject.getSubject(AccessController.getContext());

		for (Principal principal : subject.getPrincipals()) {
			if (principal instanceof CarbonPrincipal) {
				carbonPrincipal = (CarbonPrincipal) principal;
				break;
			}
		}

		if (carbonPrincipal == null) {
			return super.implies(permission);
		} else {
			try {
				return carbonPrincipal.isAuthorized((CarbonPermission) permission);
			} catch (CarbonSecurityException e) {
				log.error(e);
				return false;
			}
		}
	}

}