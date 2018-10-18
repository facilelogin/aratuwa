curl -v -X POST --basic -u $APP_CLIENTID:$APP_CLIENTSECRET -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" -k -d "grant_type=urn:ietf:params:oauth:grant-type:uma-ticket&claim_token=$IDTOKEN&ticket=$TICKET" $TOKEN_EP
echo "$STR"
echo "$STR"
echo "NEXT STEP[1]: export TOKEN=[access_token]"
echo "$STR"
echo "NEXT STEP[2]: sh 6-rs-introspect.sh"
