package ru.ifmo.rain.garder;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 1 && args.length != 4) {
            System.out.println("1 parameter or <path, sampleRate, bufferSize, overlap>");
            return;
        }

        int sampleRate;
        int bufferSize;
        int overlap;

        if (args.length == 1) {
            sampleRate = 44100;
            bufferSize = 1024 * 4;
            overlap = 768 * 4;
        } else {
            sampleRate = Integer.parseInt(args[1]);
            bufferSize = Integer.parseInt(args[1]);
            overlap = Integer.parseInt(args[2]);
        }

        String datasetRoot = args[0];
        File root = new File(datasetRoot);
        List<List<Double>> matrix = new ArrayList<>();
        for (String subNode : root.list()) {
            File directory = new File(root, subNode);
            if (directory.isDirectory()) {
                for (String sample : directory.list()) {
                    if (sample.endsWith(".wav")) {
                        System.out.println(sample);
                        File sampleFile = new File(directory, sample);
                        List<Double> features = getFeatures(sampleFile, sampleRate, bufferSize, overlap);
                        features.add(Double.valueOf(subNode));
                        matrix.add(features);
                    }
                }
            }
        }

        writeToCSV(matrix);

    }

    private static List<Double> getFeatures(File file, int sampleRate, int bufferSize, int overlap) throws Exception {
        double[][] data = new SpectrumExtractor(sampleRate, bufferSize, overlap).getRawData(file);
        List<Double> features = Arrays.stream(new BPMRawDataProcessor().processRawData(data)).boxed().collect(Collectors.toList());
        return features;
    }

    private static void writeToCSV(List<List<Double>> entities) throws Exception {

        PrintWriter pw = new PrintWriter("features.csv");
        for (List<Double> entity : entities) {
            for (int j = 0; j < entity.size(); j++) {
                if (j != 0) {
                    pw.print(",");
                }
                pw.printf(Locale.US, "%.3f", entity.get(j));
            }
            pw.println();
        }

        pw.close();
    }
}
