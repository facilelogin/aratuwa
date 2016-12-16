import os
import webbrowser

url = os.environ["SERVER_URL"] + "/authorize"
redirecturi=os.environ["REDIRECTURI"]
clientid=os.environ["CLIENTID"]
secret=os.environ["SECRET"]
headers={'Content-Type': 'application/x-www-form-urlencoded'}
scope=os.environ['SCOPE']

url = url + '?response_type=code&scope='+scope+'&redirect_uri='+redirecturi+'&client_id='+clientid

# open URL in a new tab, if a browser window is already open.
webbrowser.open_new_tab(url)
