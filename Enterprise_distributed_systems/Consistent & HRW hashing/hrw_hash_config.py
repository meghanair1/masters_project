import mmh3, socket, struct


def ip2long(ip):
    packedIP = socket.inet_aton(ip)
    return struct.unpack("!L", packedIP)[0]


def murmur(key):
    return mmh3.hash(key)


def weight(node, key):
    a = 1103515245
    b = 12345
    hash = murmur(key)
    return (a * ((a * node + b) ^ hash) + b) % (2 ^ 31)


class Ring(object):
    def __init__(self, nodes=None):
        nodes = nodes or {}
        self._nodes = set(nodes)

    def add(self, node):
        self._nodes.add(node)

    def nodes(self):
        return self._nodes

    def remove(self, node):
        self._nodes.remove(node)

    def get_node(self, key):
        assert len(self._nodes) > 0
        weights = []
        for node in self._nodes:
            n = ip2long(node)
            w = weight(n, key)
            weights.append((w, node))

        _, node = max(weights)
        return node
