import requests
import base64
import json
import os

#supress TLS related warning.
requests.packages.urllib3.disable_warnings()

url = os.environ["SERVER_URL"] + "/introspect"
username=os.environ["USERNAME"]
password=os.environ["PASSWORD"]
headers={'Authorization': 'Basic ' + base64.b64encode(username+':'+password),'Content-Type': 'application/x-www-form-urlencoded'}
access_token=os.environ["TOKEN"]
data = 'token='+access_token

response = requests.post(url, data=data,headers=headers,verify=False)
print(response.text)
