#Impersonation Connector for WSO2 Identity Server 5.2.0

* Download https://github.com/facilelogin/aratuwa/tree/master/carbon-security/org.wso2.carbon.identity.impersonation/lib/org.wso2.carbon.identity.impersonation.jar and copy it to IS_HOME/repository/components/lib. 
* Copy https://github.com/facilelogin/aratuwa/blob/master/carbon-security/org.wso2.carbon.identity.impersonation/src/main/resources/imp-user.jsp to IS_HOME/repository/deployment/server/webapps/authenticationendpoint
* Copy https://github.com/facilelogin/aratuwa/blob/master/carbon-security/org.wso2.carbon.identity.impersonation/src/main/resources/application-authentication.xml to IS_HOME/repository/conf/identity/.
* Start the Identity Server and login as an administrator.
* Main --> Identity Providers --> Add
* Select Identity-Impersonation Configuration under Federated Authenticators and enable it. 
* Give a name to the Identity Provider (say local-idp) and save it.
* Main --> Service Provider --> List --> Pick the service provider you want enable impersonation
* Under Local and Outbound Authentication Configuration --> Advanced
* Add step-1 and pick basic as the local authenticator
* Add step-2 and pick local-idp (with the impersonation authenticator) as the federated authenticator - and save the configuration.
* Create a role called impadmin and add users who can impersonate other users.
* Create a role called impuser and add users who can be impersonated
* The abolve role names can be configured from IS_HOME/repository/conf/identity/application-authentication.xml under the section identity-impersonation AuthenticationConfig.
