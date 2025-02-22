#! /usr/bin/python3

import os
import shutil

import matplotlib.pyplot as plt
import numpy as np
import pandas
from sklearn.ensemble import RandomForestClassifier
from sklearn.externals import joblib
from sklearn.model_selection import train_test_split
from sklearn.svm import SVR


def savefig(row, label):
    X = np.arange(row.size)
    plt.plot(X, row)
    plt.title('T = {}'.format(label))
    x1, x2, y1, y2 = plt.axis()
    plt.axis((x1, x2, 0, 15))
    plt.savefig('pics/temp={}____{}.png'.format(label, np.sum(row)))
    plt.close()


def predict(path, classifier, metric):
    data = pandas.read_csv(path, header=None).values
    values = data[:, :-1]
    labels = data[:, -1]

    valid_values = []
    valid_labels = []
    for row, label in zip(values, labels):
        if np.isfinite(row).all():
            valid_values.append(row)
            valid_labels.append(label)



    # for row, label in zip(values, labels):
    #     savefig(row, label)
    #
    # return 1.

    # values = values[:, :values.shape[1] // 2]

    print('number of features: {}'.format(values.shape[1]))
    print('rows before filtering: {}'.format(values.shape[0]))
    values = np.array(valid_values)
    labels = np.array(valid_labels)
    print('rows after filtering: {}'.format(values.shape[0]))

    values_train, values_test, labels_train, labels_test = train_test_split(values, labels, test_size=0.2, random_state=42)
    cls = classifier
    cls.fit(values_train, labels_train)

    predictions = cls.predict(values_test)
    print('test data: {}'.format(labels_test))
    print('predicted data: {}'.format(predictions))

    return metric(labels_test, predictions)


def avgdiff(labels_test, labels_predict):
    return np.average(np.abs(labels_test - labels_predict))


def make_classification(path):
    classifier = RandomForestClassifier(n_estimators=50, criterion='entropy')
    metric = avgdiff
    return predict(path, classifier, metric), classifier


def make_regression(path):
    classifier = SVR(kernel='linear', C=1)
    metric = avgdiff
    return predict(path, classifier, metric), classifier


def do_work(worker):
    dir = '../data/extracted_data/'
    files = os.listdir(dir)
    min_error = np.inf
    best_file = None
    best_cls = None
    for file in files:
        new_error, cls = worker(dir + file)
        print('file: {}'.format(file))
        print('average error = {}'.format(new_error))
        print('min avg error = {}'.format(min_error))
        if new_error < min_error:
            best_file = file
            min_error = new_error
            best_cls = cls

    shutil.rmtree('../data/classifier/')
    os.makedirs('../data/classifier/')
    joblib.dump(best_cls, '../data/classifier/' + best_file[0:-4] + '.pkl')
    print('min average error={} on file: {}'.format(min_error, best_file))

if __name__ == '__main__':
    do_work(make_regression)
    # do_work(make_classification)


