#Python Scripts for Testing WSO2 Identity Server REST APIs

* Edit the properties in wso2is/5.3.0/env.sh.

```
#export SERVER_URL=https://localhost:8243
export SERVER_URL=https://localhost:9443/oauth2
# WSO2 Identity Server XACML PDP endpoint
export PDP_SERVER_URL=https://localhost:9443/api/identity/entitlement/Decision/pdp
# apim2.0.0
#export CLIENTID=JynQQkdlGUhXdAqdfUTNJrqC2Qka
# is5.3.0
export CLIENTID=ywpdfeoTQqyR108aYSaBh6xqI5ga
# apim2.0.0
#export SECRET=8im2pVVQ1tjSwzZhOoVtUkSfeJ8a
# is5.3.0
export SECRET=3wa0BRg9l4yW7ZDPnTtiJHfv8yoa
# the username used for both resource owner password grant type and to authenticate to the XACML PDP
export USERNAME=admin
# the password used for both resource owner password grant type and to authenticate to the XACML PDP
export PASSWORD=admint
# the scope used in requesting an access token
export SCOPE=latest_block
# the oauth 2.0 redirect_uri used in authorization code and implicit grant types.
export REDIRECTURI=http://127.0.0.1:5000/
# set to true if we want to talk to business APIs. use with WSO2 API Manager.
export CALL_API=false
# this is to the oauth resource server. by default this runs on http://127.0.0.1:5000/
export FLASK_APP=res-server.py
# this is the business api endpoint, hosted in wso2 api manager.
export API_EP=https://localhost:8245/bc/v1.0.0/latestblock
```
* Before executing any python script, source the env.sh
```
source ./env.sh
```
* Execute the corresponding python script.
