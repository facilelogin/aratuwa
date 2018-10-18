curl -v -X POST -H "Authorization:Bearer $PAT" -H "Content-Type: application/json" -d '{"resource_scopes":["view", "update"], "description":"diabetes test results", "icon_uri":"http://www.umaas.com/icons/icon.png", "name":"http://diabetes/results/6", "type":"http://diabetes/results"}' $RES_EP -k
echo "$STR"
echo "$STR"
echo "NEXT STEP: export RESID=[_id]"
echo "$STR"
echo "NEXT STEP[2]: Update the policy with _id"
echo "$STR"
echo "NEXT STEP[3]: sh 3-rs-permission.sh"
