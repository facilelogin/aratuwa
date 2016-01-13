package org.wso2.carbon.security.login;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "login_response")
public class LoginResponse {

    @XmlAttribute(name = "username")
    @SerializedName("username")
    private String username;
    @XmlAttribute(name = "user_claims")
    @SerializedName("user_claims")
    private List<ClaimValue> claims = new ArrayList<ClaimValue>();
    @XmlAttribute(name = "roles")
    @SerializedName("roles")
    private List<String> roles = new ArrayList<String>();

    /**
     * build the login response object
     * 
     * @param username the username from the login request
     * @param roles roles of the user. this will be an empty list if the user does not have any roles or the client
     *            didn't ask for user roles.
     * @param claims the claims of the user. this will be null if the user does not have any user attributes or the
     *            client didn't ask for user attributes.
     * @return
     */
    public static LoginResponse build(String username, List<String> roles, Map<String, String> userClaims) {
        LoginResponse resp = new LoginResponse();
        resp.setUsername(username);

        if (roles != null && !roles.isEmpty()) {
            resp.setRoles(roles);
        }

        if (userClaims != null && !userClaims.isEmpty()) {
            List<ClaimValue> claimList = new ArrayList<ClaimValue>();
            for (Map.Entry<String, String> entry : userClaims.entrySet()) {
                // there can be multi-valued attributes.
                List<String> attrs = new ArrayList<String>();
                attrs.add(entry.getValue());
                claimList.add(new ClaimValue(entry.getKey(), attrs));
            }
            resp.setClaims(claimList);
        }
        return resp;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<ClaimValue> getClaims() {
        return claims;
    }

    public void setClaims(List<ClaimValue> claims) {
        this.claims = claims;
    }
}