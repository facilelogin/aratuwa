package org.wso2.carbon.identity.oauth.proxy;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.amber.oauth2.client.OAuthClient;
import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.response.OAuthClientResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.wso2.carbon.identity.oauth.proxy.util.ProxyFaultCodes;
import org.wso2.carbon.identity.oauth.proxy.util.ProxyUtils;

/**
 * this endpoint acts a proxy endpoint single page applications(spa). to authenticate a user, the spa must do a GET to
 * the /login endpoint wth spaName and code parameters. spaName is a unique identifier for each spa, and the proxy app
 * should be aware of that identifier.the proxy endpoint uses the spaName later to load the callback url corresponding
 * to the spa. the code is a random generated number by the spa. spa should gurantee its randmoness. each times the spa
 * gets rendered on the browser it has to generate the code.spas should not uses statically configured code values.
 * 
 */
@Path(ProxyUtils.PROXY_API)
@Consumes({ MediaType.APPLICATION_JSON })
@Produces(MediaType.APPLICATION_JSON)
public class LoginProxy {

    private final static Log log = LogFactory.getLog(LoginProxy.class);

    // keeps track of HttpServletRequest and HttpServletResponse
    @Context
    private MessageContext context;

    /**
     * this is the first api, the spa should call to initiate user authentication. this method will redirect the user to
     * the identity server's oauth 2.0 authorization endpoint.the value of the code parameter will be written to a
     * cookie, so it can be accessed when get redirected back from the identity server, after user authentication.
     * 
     * @param spaName paName is a unique identifier for each spa, and the proxy app should be aware of that
     *        identifier.the proxy endpoint uses the spaName later to load the callback url corresponding to the spa.
     * @param code ach times the spa gets rendered on the browser it has to generate the code.spas should not uses
     *        statically configured code values.
     * @return
     */
    @Path("login")
    @GET
    public Response getAuthzCode(@QueryParam("spaName") String spaName, @QueryParam("code") String code) {

	if (spaName == null || spaName.isEmpty()) {
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.BAD_REQUEST, ProxyFaultCodes.ERROR_002,
		    ProxyFaultCodes.Name.INVALID_INPUTS, "The value of the spaName cannot be null.");
	}

	if (code == null || code.isEmpty()) {
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.BAD_REQUEST, ProxyFaultCodes.ERROR_002,
		    ProxyFaultCodes.Name.INVALID_INPUTS, "The value of the code cannot be null.");
	}

	HttpServletResponse resp = context.getHttpServletResponse();

	// loads the client key corresponding to the spa. you do not need to have spa specific consumer keys, rather
	// can use one client key for all the spas. you get the consumer key from the identity server, at the time you
	// register the service provider, and configure it in oauth_proxy.properties file.
	String consumerKey = ProxyUtils.getConsumerKey(spaName);
	// this is the oauth 2.0 authorization endpoint of the identity server.
	String authzEndpoint = ProxyUtils.getAuthzEp();
	// get the grant type. the proxy works only with the authorization code grant type.
	String authzGrantType = ProxyUtils.getAuthzGrantType();
	// get the scope associated with the spa. each spa can define its own scopes in the oauth_proxy.properties file,
	// but in each case openid is used as a mandatory scope value.
	String scope = ProxyUtils.getScope(spaName);
	// load the callback url of the proxy. there is only one callback url. even when you create multiple service
	// providers in identity server to get multiple client key/client secret pairs, the callback url would be the
	// same.
	String callbackUrl = ProxyUtils.getCallbackUrl();

	OAuthClientRequest authzRequest = null;

	try {
	    // create a cookie under the proxy domain having code as the key and spaName as the value.
	    Cookie cookie = new Cookie(code, spaName);
	    // this cookie is only accessible by HTTPS transport.
	    cookie.setSecure(true);

	    // add cookie to the response.
	    resp.addCookie(cookie);

	    // create the oauth 2.0 request with all necessary parameters.
	    // the code passed by the spa is set as the state - so the identity server will return it back with the
	    // oauth response. we use the value of the code (or the state here) to retrieve the cookie later. this is
	    // done in a way to make this proxy app stateless.
	    authzRequest = OAuthClientRequest.authorizationLocation(authzEndpoint).setClientId(consumerKey)
		    .setRedirectURI(callbackUrl).setResponseType(authzGrantType).setScope(scope).setState(code)
		    .buildQueryMessage();
	} catch (OAuthSystemException e) {
	    log.error(e);
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.INTERNAL_SERVER_ERROR,
		    ProxyFaultCodes.ERROR_003, ProxyFaultCodes.Name.INTERNAL_SERVER_ERROR, e.getMessage());
	}

	try {
	    // redirects the user to the identity server's authorization endpoint.
	    resp.sendRedirect(authzRequest.getLocationUri());
	    return null;
	} catch (IOException e) {
	    log.error(e);
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.INTERNAL_SERVER_ERROR,
		    ProxyFaultCodes.ERROR_003, ProxyFaultCodes.Name.INTERNAL_SERVER_ERROR, e.getMessage());
	}

    }

    /**
     * 
     * @param code
     * @param state
     * @return
     */
    @Path("callback")
    @GET
    public Response handleCallback(@QueryParam("code") String code, @QueryParam("state") String state) {

	if (code == null || code.isEmpty()) {
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.BAD_REQUEST, ProxyFaultCodes.ERROR_002,
		    ProxyFaultCodes.Name.INVALID_INPUTS, "The value of the code cannot be null.");
	}
	
	if (state == null || state.isEmpty()) {
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.BAD_REQUEST, ProxyFaultCodes.ERROR_002,
		    ProxyFaultCodes.Name.INVALID_INPUTS, "The value of the state cannot be null.");
	}
	
	HttpServletResponse resp = context.getHttpServletResponse();
	HttpServletRequest req = context.getHttpServletRequest();
	Cookie[] cookies = req.getCookies();

	String spaName = null;

	// try to load the cookie corresponding to the value of the state.
	if (cookies != null && cookies.length > 0) {
	    for (int i = 0; i < cookies.length; i++) {
		if (cookies[i].getName().equals(state)) {
		    spaName = cookies[i].getValue();
		    break;
		}
	    }
	}

	if (spaName == null) {
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.BAD_REQUEST, ProxyFaultCodes.ERROR_002,
		    ProxyFaultCodes.Name.INVALID_INPUTS, "No valid cookie found.");
	}

	// loads the client key corresponding to the spa. you do not need to have spa specific consumer keys, rather
	// can use one client key for all the spas. you get the consumer key from the identity server, at the time you
	// register the service provider, and configure it in oauth_proxy.properties file.
	String consumerKey = ProxyUtils.getConsumerKey(spaName);
	// loads the client secret corresponding to the spa. you do not need to have spa specific client secret, rather
	// can use one client secret for all the spas. you get the client secret from the identity server, at the time
	// you register the service provider, and configure it in oauth_proxy.properties file.
	String consumerSecret = ProxyUtils.getConsumerSecret(spaName);
	// this is the oauth 2.0 token endpoint of the identity server.
	String tokenEndpoint = ProxyUtils.getTokenEp();
	// load the callback url of the proxy. there is only one callback url. even when you create multiple service
	// providers in identity server to get multiple client key/client secret pairs, the callback url would be the
	// same.
	String callbackUrl = ProxyUtils.getCallbackUrl();

	OAuthClientRequest accessRequest = null;

	try {
	    // create an oauth 2.0 token request.
	    accessRequest = OAuthClientRequest.tokenLocation(tokenEndpoint).setGrantType(GrantType.AUTHORIZATION_CODE)
		    .setClientId(consumerKey).setClientSecret(consumerSecret).setRedirectURI(callbackUrl).setCode(code)
		    .buildBodyMessage();
	} catch (OAuthSystemException e) {
	    log.error(e);
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.INTERNAL_SERVER_ERROR,
		    ProxyFaultCodes.ERROR_003, ProxyFaultCodes.Name.INTERNAL_SERVER_ERROR, e.getMessage());
	}

	// create an oauth 2.0 client that uses custom http client under the hood
	OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
	OAuthClientResponse oAuthResponse = null;

	try {
	    // talk to the oauth token endpoint of identity server to get the oauth access token, refresh token and id
	    // token.
	    oAuthResponse = oAuthClient.accessToken(accessRequest);
	} catch (OAuthSystemException e) {
	    log.error(e);
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.INTERNAL_SERVER_ERROR,
		    ProxyFaultCodes.ERROR_003, ProxyFaultCodes.Name.INTERNAL_SERVER_ERROR, e.getMessage());
	} catch (OAuthProblemException e) {
	    log.error(e);
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.INTERNAL_SERVER_ERROR,
		    ProxyFaultCodes.ERROR_003, ProxyFaultCodes.Name.INTERNAL_SERVER_ERROR, e.getMessage());
	}

	// read the access token from the oauth token endpoint response.
	String accessToken = oAuthResponse.getParam(ProxyUtils.ACCESS_TOKEN);
	// read the refresh token from the oauth token endpoint response.
	String refreshToken = oAuthResponse.getParam(ProxyUtils.REFRESH_TOKEN);
	// read the expiration from the oauth token endpoint response.
	long expiration = Long.parseLong(oAuthResponse.getParam(ProxyUtils.EXPIRATION));
	// read the id token from the oauth token endpoint response.
	String idToken = oAuthResponse.getParam(ProxyUtils.ID_TOKEN);

	if (idToken != null) {
	    // extract out the payload of the jwt, which comes in the id token.
	    String[] idTkElements = idToken.split(Pattern.quote("."));
	    idToken = idTkElements[1];
	}

	// create a json object aggregating oauth access token, refresh token and id token
	JSONObject json = new JSONObject();

	try {
	    json.put(ProxyUtils.ID_TOKEN, idToken);
	    json.put(ProxyUtils.ACCESS_TOKEN, accessToken);
	    json.put(ProxyUtils.REFRESH_TOKEN, refreshToken);
	    json.put(ProxyUtils.SPA_NAME, spaName);
	    json.put(ProxyUtils.EXPIRATION, new Long(expiration));
	} catch (JSONException e) {
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.INTERNAL_SERVER_ERROR,
		    ProxyFaultCodes.ERROR_003, ProxyFaultCodes.Name.INTERNAL_SERVER_ERROR, e.getMessage());
	}

	try {
	    // encrypt the json payload.
	    String encryptedCookieValue = ProxyUtils.encrypt(json.toString());
	    // create a cookie under the proxy domain with the encrypted payload. cookie name is set to the value of the
	    // code, initially passed by the spa.
	    Cookie cookie = new Cookie(state, encryptedCookieValue);
	    // the cookie is only accessible by the HTTPS transport.
	    cookie.setSecure(true);
	    // add cookie to the response.
	    resp.addCookie(cookie);
	    // get the spa callback ur. each spa has its own callback url, which is defined in the
	    // oauth_proxy.properties file
	    resp.sendRedirect(ProxyUtils.getSpaCallbackUrl(spaName));
	    return null;
	} catch (Exception e) {
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.INTERNAL_SERVER_ERROR,
		    ProxyFaultCodes.ERROR_003, ProxyFaultCodes.Name.INTERNAL_SERVER_ERROR, e.getMessage());
	}
    }

    /**
     * clears all the cookies corresponding all the service providers.
     * @param code
     * @return
     */
    @Path("logout")
    @GET
    public Response logout(@QueryParam("code") String code) {
	
	if (code == null || code.isEmpty()) {
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.BAD_REQUEST, ProxyFaultCodes.ERROR_002,
		    ProxyFaultCodes.Name.INVALID_INPUTS, "The value of the code cannot be null.");
	}

	HttpServletRequest req = context.getHttpServletRequest();
	HttpServletResponse resp = context.getHttpServletResponse();

	Cookie[] cookies = req.getCookies();
	String spaName = null;

	// try to load the cookie corresponding to the value of the code.
	if (cookies != null && cookies.length > 0) {
	    for (int i = 0; i < cookies.length; i++) {
		if (cookies[i].getName().equals(code) && spaName == null) {
		    JSONObject json;
		    try {
			json = new JSONObject(ProxyUtils.decrypt(cookies[i].getValue()));
			spaName = json.getString(ProxyUtils.SPA_NAME);
		    } catch (Exception e) {
			return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.INTERNAL_SERVER_ERROR,
				ProxyFaultCodes.ERROR_003, ProxyFaultCodes.Name.INTERNAL_SERVER_ERROR, e.getMessage());
		    }
		}
		cookies[i].setMaxAge(0);
		cookies[i].setValue("");
		resp.addCookie(cookies[i]);
	    }
	}

	if (spaName == null) {
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.INTERNAL_SERVER_ERROR,
		    ProxyFaultCodes.ERROR_004, ProxyFaultCodes.Name.SERVICE_PROVIDER_DOES_NOT_EXIST,
		    "No spa found for corresponding to the provided code");
	}

	try {
	    resp.sendRedirect(ProxyUtils.getSpaLogoutUrl(spaName));
	    return null;
	} catch (IOException e) {
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.INTERNAL_SERVER_ERROR,
		    ProxyFaultCodes.ERROR_003, ProxyFaultCodes.Name.INTERNAL_SERVER_ERROR, e.getMessage());
	}
    }

    /**
     * this is invoked by the spa to get user information. the proxy will decrypt the cookie (having the value of the
     * code parameter is its name) to extract out the user information and will send back a json response to the spa.
     * 
     * @param code this should be the same code, which is used by the spa, to talk to the /login endpoint previously.
     * @return
     */
    @Path("users")
    @GET
    public Response getUserInfo(@QueryParam("code") String code) {
	
	if (code == null || code.isEmpty()) {
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.BAD_REQUEST, ProxyFaultCodes.ERROR_002,
		    ProxyFaultCodes.Name.INVALID_INPUTS, "The value of the code cannot be null.");
	}

	HttpServletRequest req = context.getHttpServletRequest();
	Cookie[] cookies = req.getCookies();
	String encryptedCookieValue = null;

	// try to load the cookie corresponding to the value of the code.
	if (cookies != null && cookies.length > 0) {
	    for (int i = 0; i < cookies.length; i++) {
		if (cookies[i].getName().equals(code)) {
		    encryptedCookieValue = cookies[i].getValue();
		    break;
		}
	    }
	}

	if (encryptedCookieValue == null) {
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.BAD_REQUEST, ProxyFaultCodes.ERROR_002,
		    ProxyFaultCodes.Name.INVALID_INPUTS, "No valid cookie found.");
	}

	JSONObject json;
	try {
	    // decrypted ciphertext will return back a json.
	    String plainTextCookieValue = ProxyUtils.decrypt(encryptedCookieValue);
	    json = new JSONObject(plainTextCookieValue);
	    // loads the user info from the json object.
	    String userInfo = json.getString(ProxyUtils.ID_TOKEN);
	    // send back the base64url-decode user info response to the spa.
	    return Response.ok().entity(base64UrlDecode(userInfo)).build();
	} catch (Exception e) {
	    return ProxyUtils.handleResponse(ProxyUtils.OperationStatus.INTERNAL_SERVER_ERROR,
		    ProxyFaultCodes.ERROR_003, ProxyFaultCodes.Name.INTERNAL_SERVER_ERROR, e.getMessage());
	}
    }

    /**
     * base64url-decode the provided text.
     * 
     * @param base64UrlEncodedStr
     * @return
     */
    private static String base64UrlDecode(String base64UrlEncodedStr) {
	return new String(Base64.decodeBase64(base64UrlEncodedStr.getBytes()));
    }

}