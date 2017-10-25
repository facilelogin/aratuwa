curl -k -u $USERNAME:$PASSWORD  -H 'Content-Type: application/x-www-form-urlencoded' -X POST --data "token=$TOKEN"  https://localhost:9443/oauth2/introspect
