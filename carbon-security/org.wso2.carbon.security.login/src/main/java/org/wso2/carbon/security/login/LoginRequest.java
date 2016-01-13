package org.wso2.carbon.security.login;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "login_request")
public class LoginRequest {

    @XmlAttribute(name = "username")
    @SerializedName("username")
    private String username;
    @XmlAttribute(name = "password")
    @SerializedName("password")
    private String password;;
    @XmlAttribute(name = "claims")
    @SerializedName("claims")
    private List<String> claims = new ArrayList<String>();
    @XmlAttribute(name = "with_roles")
    @SerializedName("with_roles")
    private boolean withRoles;

    public boolean isWithRoles() {
        return withRoles;
    }

    public void setWithRoles(boolean withRoles) {
        this.withRoles = withRoles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getClaims() {
        return claims;
    }

    public void setClaims(List<String> claims) {
        this.claims = claims;
    }

}