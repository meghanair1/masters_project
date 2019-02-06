from concurrent import futures

import grpc
import time
import yaml

import chat_pb2 as chat
import chat_pb2_grpc as rpc

from Crypto.Cipher import AES
from Crypto import Random
from LRUCache import *
from ratelimit import limits


# Place holder for server configuration
server_config = {}


# Load server configuration
try:
    with open('../config/config.yaml', 'r') as f:
        config = yaml.load(f)
        server_config['online_users'] = config['users']
        server_config['port'] = config['port']
        server_config['cache_size'] = config['max_num_messages_per_user']
        server_config['rate_limit'] = config['max_call_per_30_seconds_per_user']
        server_config['block_size'] = config['block_size']
        server_config['groups'] = config['groups']
except:
    raise ValueError('Error reading config file. Exiting!')



# Object to hold all user related data, Cache, rate_limit and messages.
class UserChat:

    def __init__(self):
        self.messages = []

    @limits(calls=server_config['rate_limit'], period=30)
    def applyRateLimit(self):
        return ''

    def appendData(self, data):
        self.messages.append(data)

    @lrudecorator(server_config['cache_size'])
    def cache(self, message):
        return message



class ChatServer(rpc.ChatServerServicer):

    def __init__(self):


        # List with all the chat history
        self.chats = {}
        self.reqMap = {}
        self.groupMap = {}

        for group in server_config['groups']:
            for usr in server_config['groups'][group]:
                if usr not in self.groupMap:
                    self.groupMap.update({usr: set()})
                self.groupMap[usr].add(group)


    # The stream which will be used to send new messages to clients. Once connected, this stream will remain open to the client
    def ChatStream(self, data: chat.Data, context):
        user = ''
        session_group = ''
        chatting_with = []
        msgIndex = 0
        request_sent = False
        request_accepted = False

        # For every client a infinite loop starts (in gRPC's own managed thread)
        if data.connect is True:
            # Init setup for storing user message
            if data.sender not in self.chats:
                self.chats.update({data.sender: UserChat()})

            # Set the user for this grpc thread
            user = data.sender

            # Get online users
            data.onlineusers = " ".join(str(x) for x in server_config['online_users'])

            # Get user groups
            user_groups = []
            for group in server_config['groups']:
                for usr in server_config['groups'][group]:
                    if data.sender == usr:
                        user_groups.append(group)
            data.usergroups = " ".join(str(x) for x in user_groups)

            yield data

        # Keep checking for new messages any new messages
        while True:
            while len(self.chats[user].messages) > msgIndex:
                # If the flow comes here then the user has new message
                data = self.chats[user].messages[msgIndex]
                msgIndex += 1

                # If the message is not from the user who this user is not chatting with, ignore it
                if len(chatting_with) > 0 and data.sender not in chatting_with:
                    continue

                if data.connect is True:
                    if request_accepted is False and user in self.reqMap and len(self.reqMap[user]) > 0:
                        request_accepted = True
                        request_sent = True
                        self.chats[user].messages.pop(msgIndex - 1)
                        msgIndex -= 1
                        chat_request_configList = self.reqMap[user]

                        reqIndx = -1

                        for chat_request_config in chat_request_configList:
                            reqIndx += 1
                            if data.group is not '' and chat_request_config['session_group'] != data.group:
                                continue
                            if data.group is '' and chat_request_config['from'] != data.sender:
                                continue
                            break

                        if reqIndx == -1:
                            continue

                        chatting_with.append(chat_request_config['from'])

                        if data.group is not '':
                            session_group = chat_request_config['session_group']
                            chatting_with = server_config['groups'][session_group]
                            data.receiver = user

                        fetchCipherConfig(data, chat_request_config)

                        if data.group is '':
                            chat_request_configList.pop(reqIndx)

                    elif request_sent is False:
                        request_sent = True
                        request_accepted = True
                        genearteCipherConfig(data)

                        chatting_with.append(data.receiver)
                        connect_to = [data.receiver]

                        if data.group is not '':
                            session_group = data.group
                            connect_to = server_config['groups'][session_group]
                            chatting_with = server_config['groups'][session_group]

                        for usr in connect_to:
                            if usr == data.sender:
                                continue
                            if usr not in self.reqMap:
                                self.reqMap.update({usr: []})
                            self.reqMap[usr].append({'from': data.sender, 'cipher_config': data.cipherconfig, 'session_group': session_group})

                        self.chats[user].messages.pop(msgIndex - 1)
                        msgIndex -= 1
                    yield data
                elif chatting_with is not None:
                    data.connect = False
                    yield data


    def SendData(self, data: chat.Data, context):
        if data.connect is True:
            if doesSessionExist(data, self.reqMap) is True:
                return chat.Empty()
            self.chats[data.sender].appendData(data)

        send_to = [data.receiver]

        if data.group is not '':
            send_to = server_config['groups'][data.group]


        # Add it to the chat queue
        for usr in send_to:
            if usr not in self.chats:
                self.chats.update({usr: UserChat()})

        try:
            for usr in send_to:
                if usr == data.sender:
                    continue
                # self.chats[data.sender].applyRateLimit()
                data.receiver = usr
                self.chats[usr].appendData(data)
        except:
            print('Too many requests...message not sent.')
            pass
        return chat.Empty()



# Function to generate unique cipher config for a chat session
def genearteCipherConfig(data):
    data.cipherconfig.key = Random.new().read(16)
    data.cipherconfig.mode = AES.MODE_CBC
    data.cipherconfig.init_vector = Random.new().read(16)
    data.cipherconfig.block_size = server_config['block_size']


#  Function to fetch cipher config for a chat session.
def fetchCipherConfig(data, chat_request_config):
    data.cipherconfig.key = chat_request_config['cipher_config'].key
    data.cipherconfig.mode = chat_request_config['cipher_config'].mode
    data.cipherconfig.mode = chat_request_config['cipher_config'].mode
    data.cipherconfig.block_size = chat_request_config['cipher_config'].block_size


#  Function to fetch cipher config for a chat session.
def doesSessionExist(data, reqMap):
    if data.sender not in reqMap:
        return False

    chat_request_configList = reqMap[data.sender]

    for chat_request_config in chat_request_configList:
        if data.group is not '' and chat_request_config['session_group'] == data.group:
            return True
        if data.group is '' and chat_request_config['session_group'] is '' and chat_request_config['from'] == data.sender:
            return True
    return False


if __name__ == '__main__':



    # create a gRPC server
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    rpc.add_ChatServerServicer_to_server(ChatServer(), server)

    print('Spartan server started on port', str(server_config['port']))
    server.add_insecure_port('[::]:' + str(server_config['port']))
    server.start()
    # Server starts in background (another thread) so keep waiting
    while True:
        time.sleep(64 * 64 * 100)
