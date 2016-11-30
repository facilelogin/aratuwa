#Impersonation Connector for WSO2 Identity Server 5.2.0

##Deploying the connector

* Download https://github.com/facilelogin/aratuwa/blob/master/carbon-security/org.wso2.carbon.identity.impersonation/lib/org.wso2.carbon.identity.impersonation-1.0.0.jar and copy it to IS_HOME/repository/components/dropins. 
* Copy https://github.com/facilelogin/aratuwa/blob/master/carbon-security/org.wso2.carbon.identity.impersonation/src/main/resources/imp-user.jsp to IS_HOME/repository/deployment/server/webapps/authenticationendpoint
* Copy https://github.com/facilelogin/aratuwa/blob/master/carbon-security/org.wso2.carbon.identity.impersonation/src/main/resources/application-authentication.xml to IS_HOME/repository/conf/identity/.

##Creating an identity provider with the connector
* Start the Identity Server and login as an administrator.
* Main --> Identity Providers --> Add
* Select Identity-Impersonation Configuration under Federated Authenticators and enable it. 
* Give a name to the Identity Provider (say local-idp) and save it.

##Enabling impersonataion for a service provider
* Main --> Service Providers --> List --> Pick the service provider you want enable impersonation
* Under Local and Outbound Authentication Configuration --> Advanced
* Add step-1 and pick basic as the local authenticator
* Add step-2 and pick local-idp (with the impersonation authenticator) as the federated authenticator - and save the configuration.

##Define who can impersonate and who can be impersonated
* Create a role called impadmin and add users who can impersonate other users.
* Create a role called impuser and add users who can be impersonated
* The abolve role names can be configured from IS_HOME/repository/conf/identity/application-authentication.xml under the section identity-impersonation AuthenticationConfig.

##Get back the name of the impersonating user in the SAML/OIDC response
* Main --> Claims --> Add --> Add New Claim --> Select http://wso2.org/claims dialect and give http://wso2.org/claims/impersonation_admin as the claim uri and impersonation_admin as the mapped attribute and save the configuration.
* Main --> Claims --> Add --> Add New Claim --> Select http://wso2.org/oidc dialect and give impersonation_admin as the claim uri and impersonation_admin as the mapped attribute and save the configuration.
* Go to the URL https://localhost:9443/carbon/resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=/_system/config/oidc 
* Expand Properties
* Edit the property with the name openid and append impersonation_admin at the end of it's value, separated by a comma.
* Edit the service provider you want to enable the impersonation and under the Claim Configuration, under the Requested Claims, select http://wso2.org/claims/impersonation_admin. 

