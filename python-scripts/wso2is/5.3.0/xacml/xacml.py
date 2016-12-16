import requests
import base64
import json
import os

#supress TLS related warning.
requests.packages.urllib3.disable_warnings()

url = os.environ["PDP_SERVER_URL"]
username=os.environ["USERNAME"]
password=os.environ["PASSWORD"]
headers={'Authorization': 'Basic ' + base64.b64encode(username+':'+password),'Content-Type': 'application/json','Accept':'application/json'}
fh = open("xacml.req", "r")
data = fh.readline()

# prints the XACML request.
print (data)

response = requests.post(url, data=data,headers=headers,verify=False)
print(response.text)
