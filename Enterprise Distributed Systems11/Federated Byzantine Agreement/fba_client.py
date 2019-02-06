import socket, sys

port = 3000

if len(sys.argv) > 1:
    port = int(sys.argv[1])

# Create a UDP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

messages = [
    'foo:$10',
    'bar:$30',
    'foo:$20',
    'bar:$20',
    'foo:$30',
    'bar:$10'
]

server_address = ('localhost', port)

try:

    # Send data
    for message in messages:
        sent = sock.sendto(message.encode('utf-8'), server_address)

        # Receive response
        data, server = sock.recvfrom(4096)
        print(data.decode('utf-8'))

finally:
    print('Closing socket')
    sock.close()
