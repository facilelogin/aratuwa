export TOKEN_EP=https://localhost:8245/token

export AUTHZ_EP=https://localhost:8245/authorize

export REVOKE_EP=https://localhost:8245/revoke


# client id
export CLIENTID=GEYeyJtBMedfzgE5aLpCT5lF_P0a

# client secret
export CLIENTSECRET=wFlgdX7IfLEymUwiNo0Z7z_oHIQa

# the username used for both resource owner password grant type and to authenticate to the XACML PDP
export USERNAME=admin

# the password used for both resource owner password grant type and to authenticate to the XACML PDP
export PASSWORD=admin

# the scope used in requesting an access token
export SCOPE=openid

# the oauth 2.0 redirect_uri used in authorization code and implicit grant types.
export REDIRECTURI=http://127.0.0.1:5000/


# this is the business api endpoint, hosted in wso2 api manager.
export API_EP=https://localhost:8245/blocks/1.0.0/latestblock
