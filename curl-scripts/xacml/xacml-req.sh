curl -k   --basic -u  $USERNAME:$PASSWORD  -d @xacml.json --header "Content-Type:application/json" https://localhost:9443/api/identity/entitlement/decision/pdp
