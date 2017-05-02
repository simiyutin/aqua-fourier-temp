package ru.ifmo.rain.garder;

import java.util.Arrays;

public class BPMRawDataProcessor implements RawDataProcessor {
    public double[] processRawData(double[][] rawData) {
        return Arrays.stream(rawData).mapToDouble(da -> Arrays.stream(da).average().getAsDouble()).toArray();
    }
}
