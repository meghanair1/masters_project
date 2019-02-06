from hrw_hash_config import Ring
from util import get_entries_from_servers, write_output_to_file, write_entries_to_server
import sys, json
from server_config import server_list



if __name__ == '__main__':

    ring = Ring()
    ring.add(server_list[0])
    ring.add(server_list[1])
    ring.add(server_list[2])
    ring.add(server_list[3])

    write_entries_to_server(sys.argv[1], ring)

    print('Verifying the data...')

    data = get_entries_from_servers()

    print(json.dumps(data, indent=4))

    write_output_to_file('hrw_hash_output.json', data)

    print('Data verification completed. Output written to "hrw_hash_output.json"')