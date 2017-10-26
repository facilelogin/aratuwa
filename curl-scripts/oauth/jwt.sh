curl -v -X POST --basic -u $CLIENTID:$CLIENTSECRET -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" -k -d "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=$ASSERTION" $TOKEN_EP

