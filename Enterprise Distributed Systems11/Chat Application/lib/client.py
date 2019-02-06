import threading
import sys
import grpc
import chat_pb2 as chat
import chat_pb2_grpc as rpc
import time

from Crypto.Util.Padding import pad, unpad
from Crypto.Cipher import AES

address = 'localhost'
port = 3000

class Client:

    def __init__(self, sender: str):
        self.sender = sender
        self.connect = True
        self.onlineusers = []
        self.requestFrom = ''
        self.receiver = ''
        self.connect_flag = False
        self.set_prefix = True
        self.cipher_config = None
        self.myGroups = []
        self.group = ''

        # create a gRPC channel + stub
        channel = grpc.insecure_channel(address + ':' + str(port))
        self.conn = rpc.ChatServerStub(channel)

        # create new listening thread for when new message streams come in
        threading.Thread(target=self.__listen_for_messages, daemon=True).start()

        time.sleep(1)

        while self.connect_flag is False:
            continue


        print('[Spartan] User list: ' + " ".join(str(x) for x in self.onlineusers))
        print('[Spartan] Group list: ' + " ".join(str(x) for x in self.myGroups))

        if self.requestFrom is not '':
            receiver = input(self.requestFrom + ' is requesting to chat with you. Enter \'yes\' to accept or different user or group: ')
            if receiver.lower() == 'yes' or receiver == self.requestFrom:
                self.receiver = self.requestFrom
            else:
                while True:
                    if not receiver or receiver == self.sender or receiver not in self.onlineusers or receiver != self.group:
                        print('Enter a valid user or group name: ', end='', flush=True)
                        receiver = input()
                        if receiver == self.requestFrom:
                            self.receiver = receiver
                            break
                    else:
                        self.cipher_config = None
                        self.receiver = receiver
                        if self.group != receiver:
                            self.send_connect_request()
                        break
                self.requestFrom = ''
        else:
            while True:
                receiver = input('[Spartan] Enter a user or group whom you want to chat with: ')
                if not receiver or receiver == self.sender or receiver not in self.onlineusers and receiver not in self.myGroups:
                    print('Enter a valid user or group name...')
                else:
                    self.receiver = receiver
                    if self.receiver in self.myGroups:
                        self.group = self.receiver
                    self.send_connect_request()
                    break


        print('[Spartan] You are now ready to chat with ' + self.receiver + '.')

        self.openChatWindow()



    def send_connect_request(self):
        data = chat.Data()
        data.sender = self.sender
        data.receiver = self.receiver
        data.message = 'Request for connect...'.encode("utf8")
        data.connect = True
        if self.group is not '':
            data.group = self.group
        print('sending request to connect')
        self.conn.SendData(data)


    def openChatWindow(self):
        self.chat_history = []
        while True:
            if self.set_prefix is True:
                message = input("[{}] > ".format(self.sender))
            else:
                print("[{}] > ".format(self.sender), end='', flush=True)
                message = input()
            self.send_message(message)



    def __listen_for_messages(self):

        data = chat.Data()
        data.sender = self.sender
        data.receiver = self.sender
        data.connect = True

        for data in self.conn.ChatStream(data):
            if self.connect is True:
                print('[Spartan] Connected to Spartan Server at port 3000.')
                self.connect = False

            if not data:
                continue

            if len(self.onlineusers) == 0 and data.onlineusers is not '':
                self.onlineusers = data.onlineusers.split()
                self.myGroups = data.usergroups.split()

            if data.connect is True and self.sender == data.sender and self.cipher_config is None and data.cipherconfig and data.cipherconfig.key is not b'':
                self.cipher_config = data.cipherconfig

            if data.connect is True and self.sender != data.sender and self.sender == data.receiver and self.cipher_config is None:
                self.cipher_config = data.cipherconfig
                if data.group is not '':
                    self.group = data.group
                else:
                    self.requestFrom = data.sender

            if data.connect is False and ((self.sender == data.receiver and self.receiver == data.sender) or (self.group is not '' and self.receiver == data.group)):
                try:
                    print("\n[{}] {}".format(data.sender, self.decrypt(data.message)))
                    self.set_prefix = False
                    print("[{}] > ".format(self.sender), end='', flush=True)
                except:
                    pass

            self.connect_flag = True



    def send_message(self, message):
        if message is not '':
            try:
                data = chat.Data()
                data.sender = self.sender
                data.receiver = self.receiver
                data.message = self.encrypt(message)
                data.connect = False
                if self.group is not '':
                    data.group = self.group
                self.conn.SendData(data)
            except:
                pass



    def encrypt(self, message):
        encryption_suite = AES.new(self.cipher_config.key, self.cipher_config.mode, self.cipher_config.init_vector)
        return encryption_suite.encrypt(pad(message.encode("utf8"), self.cipher_config.block_size))

    def decrypt(self, message):
        decryption_suite = AES.new(self.cipher_config.key, self.cipher_config.mode, self.cipher_config.init_vector)
        return unpad(decryption_suite.decrypt(message), self.cipher_config.block_size).decode("utf8")


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Please enter your user name...')

    sender = sys.argv[1]

    Client(sender)


