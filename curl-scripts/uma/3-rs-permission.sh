curl -v -X POST -H "Authorization:Bearer $PAT" -H "Content-Type: application/json" -d '[{"resource_id":"'$RESID'","resource_scopes":["view"]}]' -k $PERM_EP
echo "$STR"
echo "$STR"
echo "NEXT STEP: export TICKET=[ticket]"
echo "$STR"
echo "NEXT STEP[2]: sh 4-app-password.sh"
