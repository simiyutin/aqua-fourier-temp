import numpy as np
import pandas
from sklearn.ensemble import RandomForestClassifier
from sklearn.svm import SVR
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
import os


def classify(path):
    data = pandas.read_csv(path, header=None).values
    values = data[:, :-1]
    labels = data[:, -1]

    valid_values = []
    valid_labels = []
    for row, label in zip(values, labels):
        if np.isfinite(row).all():
            valid_values.append(row)
            valid_labels.append(label)

    print('rows before filtering: {}'.format(values.shape[0]))
    values = np.array(valid_values)
    labels = np.array(valid_labels)
    print('rows after filtering: {}'.format(values.shape[0]))

    values_train, values_test, labels_train, labels_test = train_test_split(values, labels, test_size=0.1)

    cls = RandomForestClassifier()
    cls.fit(values_train, labels_train)

    predictions = cls.predict(values_test)
    print(predictions)
    return accuracy_score(labels_test, predictions)

if __name__ == '__main__':
    dir = '../../spectrum-extractor/calculated_features/'
    files = os.listdir(dir)
    max_accuracy = 0
    best_file = None
    for file in files:
        new_accuracy = classify(dir + file)
        print(new_accuracy)
        if new_accuracy > max_accuracy:
            best_file = file
            max_accuracy = new_accuracy

    print('max accuracy={} on file: {}'.format(max_accuracy, best_file))


