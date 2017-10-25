curl -k --user "$CLIENTID:$CLIENTSECRET" -d "code=$CODE&grant_type=authorization_code&client_id=$CLIENTID&redirect_uri=$REDIRECTURI" https://localhost:9443/oauth2/token
