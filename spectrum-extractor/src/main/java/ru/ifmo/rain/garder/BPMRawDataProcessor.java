package ru.ifmo.rain.garder;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

public class BPMRawDataProcessor implements RawDataProcessor {
    public double[] processRawData(double[][] rawData) {

        RealMatrix matrix = MatrixUtils.createRealMatrix(rawData);
        double[] result = new double[matrix.getColumnDimension()];

        for (int i = 0; i < matrix.getColumnDimension(); i++) {
            result[i] = new Mean().evaluate(matrix.getColumn(i));
        }

        return result;
    }
}
