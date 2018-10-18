curl -v -X POST --basic -u $APP_CLIENTID:$APP_CLIENTSECRET -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" -k -d "grant_type=password&username=$REQ_USERNAME&password=$REQ_PASSWORD&scope=openid" $TOKEN_EP
echo "$STR"
echo "$STR"
echo "NEXT STEP: export IDTOKEN=[id_token]"
echo "$STR"
echo "NEXT STEP[2]: sh 5-app-rpt.sh"
