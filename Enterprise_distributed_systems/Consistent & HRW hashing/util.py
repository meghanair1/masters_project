import requests, json, hashlib, csv
from server_config import server_list, server_map


def get_request(server):
    url = server + '/api/v1/entries'
    headers = {'content-type': 'application/json'}
    return requests.get(url, headers=headers)


def send_request(server, data):
    url = server + '/api/v1/entries'
    headers = {'content-type': 'application/json'}
    return requests.post(url, headers=headers, data=json.dumps(data))


def get_entries_from_servers():
    data = []
    for server in server_list:
        get_key = 'GET ' + server_map[server] + '/api/v1/entries'
        res = get_request(server_map[server])
        data.append({get_key: res.json()})
    return data


def write_output_to_file(file_name, data):
    with open(file_name, 'w') as outfile:
        json.dump(data, outfile)


def write_entries_to_server(input_file, ring):
    with open(input_file) as f:

        reader = csv.DictReader(f)
        count = 0

        for row in reader:
            count += 1
            hsh = hashlib.sha256()
            key = "{}:{} {}:{}".format(row['Year'], row['113 Cause Name'],
                                       row['Cause Name'], row['State']).encode('utf-8')
            hsh.update(key)
            hash_key = hsh.hexdigest()
            value = "{},{},{},{},{},{}".format(row['Year'], row['113 Cause Name'], row['Cause Name'], row['State'],
                                               row['Deaths'], row['Age-adjusted Death Rate'])
            server = ring.get_node(hash_key)
            res = send_request(server_map[server], {hash_key: value})
            print(res.status_code, res.reason)
            print('Total records inserted... ', count)

        print('Uploaded all ' + str(count) + ' entries.')
