import traceback
from os import listdir
from os.path import isfile, join
from flask import Flask, request, jsonify
import numpy as np
from sklearn.externals import joblib

app = Flask(__name__)



@app.route('/get_params')
def hello():
    return get_classifier_file()[:-4]

@app.route('/', methods=['POST'])
def predict():
    if clf:
        try:
            with open('log.txt', 'w') as log_file:
                log_file.write(str(request.data))
            query = np.fromstring(request.data, dtype=float, sep=' ')
            prediction = clf.predict(np.array(query).reshape(1, -1))
            return str(prediction[0])
        except Exception as e:
            return jsonify({'error': str(e), 'trace': traceback.format_exc()})
    else:
        return 'no model here'


def get_classifier_file():
    curr_dir = "."
    pkl_file = [f for f in listdir(curr_dir) if isfile(join(curr_dir, f)) and f.endswith('.pkl')]
    return pkl_file[0]


if __name__ == '__main__':
    class Classifier:
        def __init__(self):
            pass

        @staticmethod
        def predict(test_data):
            pkl_file = get_classifier_file()
            cls = joblib.load(pkl_file)
            return cls.predict(test_data)


    clf = Classifier()
    app.run(host='0.0.0.0')

