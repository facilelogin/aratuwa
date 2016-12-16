import requests
import base64
import json
import os

#supress TLS related warning.
requests.packages.urllib3.disable_warnings()

def base64_url_decode(inp):
    padding_factor = (4 - len(inp) % 4) % 4
    inp += "="*padding_factor
    return base64.b64decode(unicode(inp).translate(dict(zip(map(ord, u'-_'), u'+/'))))

url = os.environ["SERVER_URL"] + "/token"
clientid=os.environ["CLIENTID"]
secret=os.environ["SECRET"]
headers={'Authorization': 'Basic ' + base64.b64encode(clientid+':'+secret),'Content-Type': 'application/x-www-form-urlencoded'}
assertion=os.environ["JWT"]
data = 'grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion='+assertion

response = requests.post(url, data=data,headers=headers,verify=False)
print(response.text)
