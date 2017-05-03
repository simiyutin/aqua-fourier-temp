package ru.spbau.aquafourier.extractor;

import java.util.Arrays;

public class BPMRawDataProcessor implements RawDataProcessor {
    public double[] processRawData(double[][] rawData) {
//        return Arrays.stream(rawData).mapToDouble(da -> Arrays.stream(da).average().getAsDouble()).toArray();
        double[] averages = new double[rawData.length];
        for (int i = 0; i < rawData.length; i++) {
            double sum = 0;
            for (int j = 0; j < rawData[i].length; j++) {
                sum += rawData[i][j];
            }
            averages[i] = sum / rawData[i].length;
        }
        return averages;
    }
}
