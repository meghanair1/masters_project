import socket, sys, pickledb, threading, struct, json, time

port = 3000
multicast_recv_group = ('224.3.29.71')
multicast_send_group = ('224.3.29.71', 10000)
multicast_recv_server_address = ('', 10000)
cluster = [3000, 3001, 3002, 3003]

MIN_QUORUM = 3

cache = {}

if len(sys.argv) > 1:
    port = int(sys.argv[1])

db = pickledb.load('assignment3_' + str(port) + '.db', False)


class Server(threading.Thread):

    def __init__(self, threadID, name, flag):
        threading.Thread.__init__(self)
        self.flag = flag

    def run(self):
        if self.flag is 1:
            startClientServer()
        else:
            startMulticastServer()


def startClientServer():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    server_address = ('localhost', port)
    log_message('Starting client server communication on %s port %s' % server_address)
    sock.bind(server_address)
    receiveMessageFromClient(sock)


def startMulticastServer():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEPORT, 1)
    except AttributeError:
        pass
        sock.setsockopt(socket.SOL_IP, socket.IP_MULTICAST_TTL, 20)
        sock.setsockopt(socket.SOL_IP, socket.IP_MULTICAST_LOOP, 1)

    log_message('Starting server to server communication on %s port %s' % multicast_send_group)

    sock.bind(multicast_recv_server_address)
    group = socket.inet_aton(multicast_recv_group)
    mreq = struct.pack('4sL', group, socket.INADDR_ANY)
    sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)
    receiveMessageFromCluster(sock)


def sendMessageToCluster(message):
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.settimeout(0.2)
    ttl = struct.pack('b', 1)
    sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, ttl)
    try:
        ballot = message
        sock.sendto(json.dumps(message).encode('utf-8'), multicast_send_group)
        # Look for responses from all recipients
        while True:
            try:
                data, server = sock.recvfrom(4096)
                recv_ballot = json.loads(data.decode('utf-8'))
                for p in recv_ballot['votes']:
                    if p not in ballot['votes']:
                        ballot['votes'].append(p)
            except socket.timeout:
                break
    finally:
        sock.close()
    return ballot


def receiveMessageFromCluster(sock):
    while True:
        message, address = sock.recvfrom(4096)
        ballot = json.loads(message.decode('utf-8'))

        if len(ballot['votes']) == 4:
            continue

        log_message('Received ballot from {0}'.format(ballot['src_node']))
        log_message('Sending ballot for voting by other nodes.')
        ballot = vote(ballot)
        sock.sendto(json.dumps(ballot).encode('utf-8'), address)


def receiveMessageFromClient(sock):
    while True:
        message, address = sock.recvfrom(4096)
        if message:
            message = message.decode('utf-8')
            initiateBallot(message)
            sock.sendto('{0} acknowledged by {1}. FBA initiated.'.format(message, port).encode('utf-8'), address)


def initiateBallot(message):
    ballot = {
        'cluster': cluster,
        'src_node': port,
        'message': message,
        'state': 'Pending',
        'votes': []
    }
    return vote(ballot)


def vote(ballot):
    log_message('Node {0} voted for {1}'.format(port, ballot['message']))
    ballot['src_node'] = port
    ballot['votes'].append(port)

    recv_ballot = sendMessageToCluster(ballot)
    log_message('Received voting result from other nodes')

    if len(recv_ballot['votes']) >= 3:
        unique_votes = []
        for v in recv_ballot['votes']:
            if v not in unique_votes:
                unique_votes.append(v)
        if len(unique_votes) >= 3:
            log_message('Consensus reached by {0} nodes (MIN_QUORUM=3)'.format(MIN_QUORUM))
            log_message('Changing state of ballot to "Accept"')
            recv_ballot['state'] = 'Accept'

    if recv_ballot['state'] is 'Accept':
        writeToLocalDB(recv_ballot['message'])
    else:
        log_message('Consensus not reached. Aborting!')
    return recv_ballot


def writeToLocalDB(message):
    if message in cache:
        return
    else:
        log_message('Writing data to PickleDB...')
        tokens = message.split(':')
        cache.update({message: message})
        db.set(tokens[0], tokens[1])
        db.dump()
        log_message('****** Updated data on node {0} ******'.format(port))
        key_set = db.getall()
        for key in key_set:
            log_message('{0} -> {1}'.format(key, db.get(key)))


def log_message(message):
    log = open('output_' + str(port) + '.txt', 'a')
    print(message)
    log.write(message)
    log.write('\n')
    log.close()


# Create new threads
client_daemon = Server(1, "client_daemon", 1)
fba_daemon = Server(2, "fba_daemon", 2)

# Start new Threads
client_daemon.start()
fba_daemon.start()
