import requests
import base64
import os

#supress TLS related warning.
requests.packages.urllib3.disable_warnings()

url = os.environ["SERVER_URL"] + "/token"
clientid=os.environ["CLIENTID"]
secret=os.environ["SECRET"]
headers={'Authorization': 'Basic ' + base64.b64encode(clientid+':'+secret),'Content-Type': 'application/x-www-form-urlencoded'}
assertion=os.environ["JWT"]
data = 'grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion='+assertion

response = requests.post(url, data=data,headers=headers,verify=False)
print(response.text)
