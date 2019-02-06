from flask import Flask, request
from flask_restful import Api, Resource
import sys

app = Flask(__name__)
api = Api(app)

data_store = []
custom_port = 5000  # default port


class EntryList(Resource):
    def post(self):
        data_store.append(request.get_json())
        return '', 201

    def get(self):
        return {
            'num_entries': len(data_store),
            'entries': data_store
        }, 200


api.add_resource(EntryList, '/api/v1/entries')

if __name__ == '__main__':
    if len(sys.argv) > 1:
        custom_port = sys.argv[1]

    app.run(host='localhost', port=custom_port, debug=True)
