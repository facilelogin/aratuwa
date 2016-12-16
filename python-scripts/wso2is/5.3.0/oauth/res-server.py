
from flask import Flask
from flask import request
import os
import base64
import requests
import json

def base64_url_decode(inp):
    padding_factor = (4 - len(inp) % 4) % 4
    inp += "="*padding_factor
    return base64.b64decode(unicode(inp).translate(dict(zip(map(ord, u'-_'), u'+/'))))

app = Flask(__name__)

@app.route('/')
def callback():
    code = request.args.get('code')
    if (code is not None):
        url = os.environ["SERVER_URL"] + '/token'
        clientid=os.environ["CLIENTID"]
        redirecturi=os.environ["REDIRECTURI"]
        secret=os.environ["SECRET"]
        headers={'Authorization': 'Basic ' + base64.b64encode(clientid+':'+secret),'Content-Type': 'application/x-www-form-urlencoded'}
        scope=os.environ['SCOPE']
        data = 'grant_type=authorization_code&client_id='+clientid+'&code='+code+'&redirect_uri='+redirecturi

        response = requests.post(url, data=data,headers=headers,verify=False)
        json_data=json.loads(response.text)

        access_token=json_data['access_token']
        print('Access Token: ' + access_token)

        print(json_data)
        jwt=json_data['id_token']
        print(jwt)
        jwt = jwt.split('.')

        return  base64_url_decode(jwt[1])
    else:
        return 'Unsupported grant tyoe'
