import urllib.request
import urllib.error
import json

url = "https://restcountries.com/v3.1/all?fields=name,cca3,capital,region,population,flags,area"

print(f"Testing URL: {url}")

try:
    req = urllib.request.Request(url)
    req.add_header('User-Agent', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36')
    
    with urllib.request.urlopen(req) as response:
        print(f"Status Code: {response.getcode()}")
        data = response.read()
        try:
            json_data = json.loads(data)
            print("Successfully parsed JSON.")
            print(f"Number of countries found: {len(json_data)}")
            if len(json_data) > 0:
                print("First country sample:")
                print(json.dumps(json_data[0], indent=2))
        except json.JSONDecodeError:
            print("Failed to parse JSON response")
            print("Raw response start:", data[:200])

except urllib.error.HTTPError as e:
    print(f"HTTP Error: {e.code}")
    print(e.read().decode('utf-8'))
except urllib.error.URLError as e:
    print(f"URL Error: {e.reason}")
except Exception as e:
    print(f"General Error: {e}")
