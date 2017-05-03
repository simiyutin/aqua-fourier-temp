package ru.ifmo.rain.garder;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

public class AverageFrequencyDataProcessor implements RawDataProcessor {
    @Override
    public double[] processRawData(double[][] rawData) {

        RealMatrix matrix = MatrixUtils.createRealMatrix(rawData).transpose();
        double[] result = new double[matrix.getRowDimension()];

        for (int i = 0; i < matrix.getRowDimension(); i++) {
            result[i] = new Mean().evaluate(matrix.getRow(i));
        }

        return result;
    }
}
