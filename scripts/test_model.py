import os
import pickle
import numpy as np

import pandas
from sklearn.externals import joblib


def avgdiff(labels_test, labels_predict):
    return np.average(np.abs(labels_test - labels_predict))


if __name__ == '__main__':
    cls_path = '../data/classifier/'
    cls_file = os.listdir(cls_path)[0]
    print('cls file: {}'.format(cls_path + cls_file))
    cls = joblib.load(cls_path + cls_file)

    dir = '../test_data/extracted_data/'
    files = os.listdir(dir)

    for file in files:
        print(dir + file)
        data = pandas.read_csv(dir + file, header=None).values
        values = data[:, :-1]
        # values = values[:, :values.shape[1] // 2]

        labels = data[:, -1]

        predictions = cls.predict(values)
        print('test data: {}'.format(labels))
        print('predicted data: {}'.format(predictions))
        print('average error: {}'.format(avgdiff(labels, predictions)))
