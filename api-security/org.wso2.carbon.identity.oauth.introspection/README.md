#OAuth 2.0 Token Introspection API for WSO2 Identity Server

* Checkout and build the code from https://github.com/facilelogin/aratuwa/tree/master/api-security/org.wso2.carbon.identity.oauth.introspection  and deploy it as a war file in IS 5.1.0 (IS_HOME/repository/deployment/server/webapps/). 
* Restart the Identity Server and now you should be able to use the introspection API.
* Find below the usage of the introspection API. 

 ```javascript
     Empty Token:
     
     curl -k -H 'Content-Type: application/x-www-form-urlencoded' -X POST --data 'token=' https://localhost:9443/introspect
     
     Response: {"active":false} 
```
 ```javascript
     Invalid Token 

     curl -k -H 'Content-Type: application/x-www-form-urlencoded' -X POST --data 'token=Bjhk98792k9hkjhk' https://localhost:9443/introspect 

     Response: {"active":false,"token_type":"bearer"} 
```
