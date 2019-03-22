package org.wso2.carbon.identity.oauth.introspection;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.dto.OAuth2IntrospectionResponseDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;

@Path("/")
@Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
@Produces(MediaType.APPLICATION_JSON)
public class IntrospectResource {

    private final static Log log = LogFactory.getLog(IntrospectResource.class);
    private final static String DEFAULT_TOKEN_TYPE_HINT = "bearer";
    private final static String DEFAULT_TOKEN_TYPE = "Bearer";
    private final static String JWT_TOKEN_TYPE = "JWT";

    /**
     * 
     * @param token The string value of the token. For access tokens, this is the "access_token" value returned from the
     *        token end-point defined in OAuth 2.0 [RFC6749], Section 5.1. For refresh tokens, this is the
     *        "refresh_token" value returned from the token end-point as defined in OAuth 2.0 [RFC6749], Section 5.1.
     *        Other token types are outside the scope of this specification.
     * @return
     */
    @POST
    public Response introspect(@FormParam("token") String token) {
	// if no token type hint provided, use the default one: bearer.
	// this is just a workaround. ideally findOAuthConsumerIfTokenIsValid should return back the token type, when
	// token_type_hint is not specified.
	return introspect(token, DEFAULT_TOKEN_TYPE_HINT);
    }

    /**
     * 
     * @param token The string value of the token. For access tokens, this is the "access_token" value returned from the
     *        token end-point defined in OAuth 2.0 [RFC6749], Section 5.1. For refresh tokens, this is the
     *        "refresh_token" value returned from the token end-point as defined in OAuth 2.0 [RFC6749], Section 5.1.
     *        Other token types are outside the scope of this specification.
     * @param tokenTypeHint A hint about the type of the token submitted for introspection. The protected resource MAY
     *        pass this parameter to help the authorization server optimize the token lookup. If the server is unable to
     *        locate the token using the given hint, it MUST extend its search across all of its supported token types.
     *        An authorization server MAY ignore this parameter, particularly if it is able to detect the token type
     *        automatically. Values for this field are defined in the "OAuth Token Type Hints" registry defined in OAuth
     *        Token Revocation [RFC7009].
     * @return
     */
    @POST
    public Response introspect(@FormParam("token") String token, @FormParam("token_type_hint") String tokenTypeHint) {

	OAuth2TokenValidationRequestDTO request;
	OAuth2IntrospectionResponseDTO response;

	if (tokenTypeHint == null) {
		tokenTypeHint = DEFAULT_TOKEN_TYPE_HINT;
	}

	if (log.isDebugEnabled()) {
	    log.debug("Token type hint: " + tokenTypeHint);
	}

	if (token == null || token.trim().length() == 0) {
	    // Note that a properly formed and authorized query for an inactive or otherwise invalid token (or a token
	    // the protected resource is not allowed to know about) is not considered an error response by this
	    // specification.
	    return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Invalid input\"}").build();
	}

	// first we need to validate the access token against the OAuth2TokenValidationService OSGi service.

	request = new OAuth2TokenValidationRequestDTO();
	OAuth2TokenValidationRequestDTO.OAuth2AccessToken accessToken = request.new OAuth2AccessToken();
	accessToken.setIdentifier(token);
	accessToken.setTokenType(tokenTypeHint);
	request.setAccessToken(accessToken);

	// get a reference to the OAuth2TokenValidationService OSGi service.
	OAuth2TokenValidationService tokenService = (OAuth2TokenValidationService) PrivilegedCarbonContext
		.getThreadLocalCarbonContext().getOSGiService(OAuth2TokenValidationService.class);

	response = tokenService.buildIntrospectionResponse(request);

	if (response.getError() != null) {
	    if (log.isDebugEnabled()) {
		log.debug("The error why token is made inactive: " + response.getError());
	    }
	    // the client needs not to know about why exactly the token is not active.
	    return Response.status(Response.Status.OK).entity("{\"active\":false}").build();
	}

	IntrospectionResponseBuilder respBuilder = new IntrospectionResponseBuilder().setActive(response.isActive())
		.setNotBefore(response.getNbf()).setScope(response.getScope()).setUsername(response.getUsername())
		.setTokenType(DEFAULT_TOKEN_TYPE).setClientId(response.getClientId()).setIssuedAt(response.getIat())
		.setExpiration(response.getExp());

		if (StringUtils.equalsIgnoreCase(tokenTypeHint, JWT_TOKEN_TYPE)) {
	    // we need to handle JWT token differently.
	    // the introspection response has parameters specific to the JWT token.
	    respBuilder.setAudience(response.getAud()).setJwtId(response.getJti()).setSubject(response.getSub())
		    .setIssuer(response.getIss());
	}

	try {
	    return Response.ok(respBuilder.build(), MediaType.APPLICATION_JSON).status(Response.Status.OK).build();
	} catch (JSONException e) {
	    log.error("Error occured while building the json response.", e);
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
		    .entity("{'error': 'Error occured while building the json response.'}").build();
	}
    }
}