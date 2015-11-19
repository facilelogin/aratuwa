package org.wso2.carbon.identity.oauth.introspection;

import java.util.HashMap;
import java.util.Map;

import org.apache.oltu.oauth2.common.utils.JSONUtils;
import org.codehaus.jettison.json.JSONException;

/**
 * 
 * this class is responsible for building the introspection response.
 * 
 */
public class IntrospectionResponseBuilder {

    private Map<String, Object> parameters = new HashMap<String, Object>();
    private boolean isActive = false;

    /**
     * build the introspection response.
     * 
     * @return
     * @throws JSONException
     */
    public String build() throws JSONException {
	return JSONUtils.buildJSON(parameters);
    }

    /**
     * 
     * @param isActive
     * @return
     */
    public IntrospectionResponseBuilder setActive(boolean isActive) {
	parameters.put(IntrospectionResponse.ACTIVE, isActive);
	if (!isActive) {
	    // if the token is not active we do not want to return back the expiration time.
	    if (parameters.containsKey(IntrospectionResponse.EXP)) {
		parameters.remove(IntrospectionResponse.EXP);
	    }
	    // if the token is not active we do not want to return back the nbf time.
	    if (parameters.containsKey(IntrospectionResponse.NBF)) {
		parameters.remove(IntrospectionResponse.NBF);
	    }
	}
	this.isActive = isActive;
	return this;
    }

    /**
     * 
     * @param issuedAt
     * @return
     */
    public IntrospectionResponseBuilder setIssuedAt(long issuedAt) {
	if (issuedAt != 0) {
	    parameters.put(IntrospectionResponse.IAT, issuedAt);
	}
	return this;
    }

    /**
     * 
     * @param jwtId
     * @return
     */
    public IntrospectionResponseBuilder setJwtId(String jwtId) {
	if (jwtId != null && !jwtId.isEmpty()) {
	    parameters.put(IntrospectionResponse.JTI, jwtId);
	}
	return this;
    }

    /**
     * 
     * @param subject
     * @return
     */
    public IntrospectionResponseBuilder setSubject(String subject) {
	if (subject != null && !subject.isEmpty()) {
	    parameters.put(IntrospectionResponse.SUB, subject);
	}
	return this;
    }

    /**
     * 
     * @param expiration
     * @return
     */
    public IntrospectionResponseBuilder setExpiration(long expiration) {
	if (isActive && expiration != 0) {
	    // if the token is not active we do not want to return back the expiration time.
	    parameters.put(IntrospectionResponse.EXP, expiration);
	}
	return this;
    }

    /**
     * 
     * @param username
     * @return
     */
    public IntrospectionResponseBuilder setUsername(String username) {
	if (username != null && !username.isEmpty()) {
	    parameters.put(IntrospectionResponse.USERNAME, username);
	}
	return this;
    }

    /**
     * 
     * @param tokenType
     * @return
     */
    public IntrospectionResponseBuilder setTokenType(String tokenType) {
	if (tokenType != null && !tokenType.isEmpty()) {
	    parameters.put(IntrospectionResponse.TOKEN_TYPE, tokenType);
	}
	return this;
    }

    /**
     * 
     * @param notBefore
     * @return
     */
    public IntrospectionResponseBuilder setNotBefore(long notBefore) {
	if (isActive && notBefore != 0) {
	    // if the token is not active we do not want to return back the nbf time.
	    parameters.put(IntrospectionResponse.NBF, notBefore);
	}
	return this;
    }

    /**
     * 
     * @param audience
     * @return
     */
    public IntrospectionResponseBuilder setAudience(String audience) {
	if (audience != null && !audience.isEmpty()) {
	    parameters.put(IntrospectionResponse.AUD, audience);
	}
	return this;
    }

    /**
     * 
     * @param issuer
     * @return
     */
    public IntrospectionResponseBuilder setIssuer(String issuer) {
	if (issuer != null && !issuer.isEmpty()) {
	    parameters.put(IntrospectionResponse.ISS, issuer);
	}
	return this;
    }

    /**
     * 
     * @param scope
     * @return
     */
    public IntrospectionResponseBuilder setScope(String scope) {
	if (scope != null && !scope.isEmpty()) {
	    parameters.put(IntrospectionResponse.SCOPE, scope);
	}
	return this;
    }

    /**
     * 
     * @param consumerKey
     * @return
     */
    public IntrospectionResponseBuilder setClientId(String consumerKey) {
	if (consumerKey != null && !consumerKey.isEmpty()) {
	    parameters.put(IntrospectionResponse.CLIENT_ID, consumerKey);
	}
	return this;
    }

    /**
     * 
     * @param errorCode
     * @return
     */
    public IntrospectionResponseBuilder setErrorCode(String errorCode) {
	parameters.put(IntrospectionResponse.Error.ERROR, errorCode);
	return this;
    }

    /**
     * 
     * @param description
     * @return
     */
    public IntrospectionResponseBuilder setErrorDescription(String description) {
	parameters.put(IntrospectionResponse.Error.ERROR_DESCRIPTION, description);
	return this;
    }
}
