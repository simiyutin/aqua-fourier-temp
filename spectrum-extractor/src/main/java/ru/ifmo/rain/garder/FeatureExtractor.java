package ru.ifmo.rain.garder;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureExtractor {

    public static final int SAMPLE_RATE_DEFAULT = 44100;
    public static final int BUFFER_SIZE_DEFAULT = 1024 * 4;
    public static final int OVERLAP_DEFAULT = 768 * 4;

    public static List<Double> extract(File sampleFile, int sampleRate, int bufferSize, int overlap) throws Exception {
        return getFeatures(sampleFile, sampleRate, bufferSize, overlap);
    }

    public static List<Double> extract(File sampleFile) throws Exception {
        return getFeatures(sampleFile, SAMPLE_RATE_DEFAULT, BUFFER_SIZE_DEFAULT, OVERLAP_DEFAULT);
    }

    private static List<Double> getFeatures(File file, int sampleRate, int bufferSize, int overlap) throws Exception {
        double[][] data = new SpectrumExtractor(sampleRate, bufferSize, overlap).getRawData(file);
        List<Double> features = Arrays.stream(new BPMRawDataProcessor().processRawData(data)).boxed().collect(Collectors.toList());
        return features;
    }

}
