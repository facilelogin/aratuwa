#export SERVER_URL=https://localhost:8245
export SERVER_URL=https://localhost:9443/oauth2
# WSO2 Identity Server XACML PDP endpoint
export PDP_ERVER_URL=https://localhost:9443/api/identity/entitlement/Decision/pdp
# API Manager 2.0.0
#export CLIENTID=JynQQkdlGUhXdAqdfUTNJrqC2Qka
# IS 5.3.0
export CLIENTID=ywpdfeoTQqyR108aYSaBh6xqI5ga
# IS 5.2.0
#export CLIENTID=is7XR8N0wRiPfdRXFYEfUzqJhXMa
# API Manager 2.0.0
#export SECRET=8im2pVVQ1tjSwzZhOoVtUkSfeJ8a
#IS 5.3.0
export SECRET=3wa0BRg9l4yW7ZDPnTtiJHfv8yoa
#IS 5.2.0
#export SECRET=Tol7ge2YVVr0sNvwq0Nsfrhb3CYa
export USERNAME=admin
export PASSWORD=admin
export SCOPE=latest_block
export REDIRECTURI=http://127.0.0.1:5000/
# set to true if we want to talk to business APIs. Use with WSO2 API Manager.
export CALL_API=false
export FLASK_APP=res-server.py
