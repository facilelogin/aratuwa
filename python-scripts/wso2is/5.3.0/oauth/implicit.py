import requests
import base64
import json
import os
import webbrowser

def base64_url_decode(inp):
    padding_factor = (4 - len(inp) % 4) % 4
    inp += "="*padding_factor
    return base64.b64decode(unicode(inp).translate(dict(zip(map(ord, u'-_'), u'+/'))))


url = os.environ["SERVER_URL"] + "/authorize"
redirecturi=os.environ["REDIRECTURI"]
clientid=os.environ["CLIENTID"]
headers={'Content-Type': 'application/x-www-form-urlencoded'}
scope=os.environ['SCOPE']
url = url + '?response_type=token&scope='+scope+'&redirect_uri='+redirecturi+'&client_id='+clientid


# open URL in a new tab, if a browser window is already open.
webbrowser.open_new_tab(url)
