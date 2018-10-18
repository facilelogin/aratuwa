curl -v -X POST --basic -u $CLIENTID:$CLIENTSECRET -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" -k -d "grant_type=password&username=$USERNAME&password=$PASSWORD&scope=$SCOPE" $TOKEN_EP
echo "$STR"
echo "$STR"
echo "NEXT STEP [1]: export PAT=[access_token]"
echo "$STR"
echo "NEXT STEP [2]: Check resource name"
echo "$STR"
echo "NEXT STEP[3]: sh 2-rs-resource-rg.sh"
