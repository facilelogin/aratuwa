package org.wso2.carbon.security.auth.module;

public class CarbonSecurityException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8652544420899726990L;

	public CarbonSecurityException(String message, Throwable exp) {
		super(message, exp);
	}

	/**
	 * 
	 * @param exp
	 */
	public CarbonSecurityException(Throwable exp) {
		super(exp);
	}
}
