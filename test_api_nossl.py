import urllib.request
import urllib.error
import ssl
import json

# Ignore SSL certificate errors
ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

url = "https://restcountries.com/v3.1/all?fields=name,cca3,capital,region,population,flags,area"

print(f"Testing URL: {url}")

try:
    req = urllib.request.Request(url)
    req.add_header('User-Agent', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36')
    
    with urllib.request.urlopen(req, context=ctx) as response:
        print(f"Status Code: {response.getcode()}")
        data = response.read()
        try:
            json_data = json.loads(data)
            print("Successfully parsed JSON.")
            print(f"Number of items: {len(json_data)}")
        except json.JSONDecodeError:
            print("Failed to parse JSON")

except urllib.error.HTTPError as e:
    print(f"HTTP Error: {e.code}")
    print(e.read().decode('utf-8'))
except Exception as e:
    print(f"Error: {e}")
