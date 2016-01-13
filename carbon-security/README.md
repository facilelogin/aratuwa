#WSO2 Carbon Security Samples

## 1. A lightweight login API

* Build the API from https://github.com/facilelogin/aratuwa/tree/master/carbon-security/org.wso2.carbon.security.login

* Copy the artifact(login.war) created from the above step to IS_HOME/repository/deployment/server/webapps

* Restart the WSO2 Identity Server and make sure the login.war is deployed properly.

* Following is an example cURL request just to authenticate a user.

```javascript
curl -k -v  -H "Content-Type: application/json"  -X POST -d @auth_req.json https://localhost:9443/login
```

```javascript
auth_req.json:

{ 
   "username": "admin",
   "password": "admin"
}
```
```javascript
Response:

HTTP/1.1 200 OK
{  
   "username":"admin",
   "user_claims":[  

   ],
   "roles":[  

   ]
}
```

* Following is an example cURL request to authenticate a user and get all his roles.

```javascript
curl -k -v  -H "Content-Type: application/json"  -X POST -d @auth_req.json https://localhost:9443/login
```

```javascript
auth_req.json:

{  
   "username": "admin",
   "password": "admin",
   "with_roles": true
}
```
```javascript
Response:

HTTP/1.1 200 OK
{  
   "username":"admin",
   "user_claims":[  

   ],
   "roles":[  
      "admin",
      "Application/oauth2-proxy",
      "Internal/everyone"
   ]
}
```

* Following is an example cURL request to authenticate a user and get all his roles and a selected set of claims.

```javascript
curl -k -v  -H "Content-Type: application/json"  -X POST -d @auth_req.json https://localhost:9443/login
```

```javascript
auth_req.json:

{  
   "username": "admin",
   "password": "admin",
   "with_roles" : true,
   "claims" : ["http://wso2.org/claims/emailaddress"]
}

```
```javascript
Response:

HTTP/1.1 200 OK
{  
   "username":"admin",
   "user_claims":[  
      {  
         "claim_uri":"http://wso2.org/claims/emailaddress",
         "value":[  
            "admin@wso2.com"
         ]
      }
   ],
   "roles":[  
      "admin",
      "Application/oauth2-proxy",
      "Internal/everyone"
   ]
}
```
