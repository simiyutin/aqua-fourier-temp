package ru.ifmo.rain.garder;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 2 && args.length != 5) {
            System.out.println(String.format(
                    "required <path, max_files_for_class> or <path, max_files_for_class, sampleRate, bufferSize, overlap>" +
                            ", got %s", Arrays.toString(args)));
            return;
        }

        int sampleRate;
        int bufferSize;
        int overlap;

        if (args.length == 2) {
            sampleRate = FeatureExtractor.SAMPLE_RATE_DEFAULT;
            bufferSize = FeatureExtractor.BUFFER_SIZE_DEFAULT;
            overlap = FeatureExtractor.OVERLAP_DEFAULT;
        } else {
            sampleRate = Integer.parseInt(args[2]);
            bufferSize = Integer.parseInt(args[3]);
            overlap = Integer.parseInt(args[4]);
        }


        String datasetRoot = args[0];
        int maxFiles = Integer.parseInt(args[1]);
        System.out.println(String.format("root: %s", datasetRoot));
        System.out.println(String.format("maxFiles: %s", maxFiles));
        System.out.println(String.format("sampleRate: %s", sampleRate));
        System.out.println(String.format("bufferSize: %s", bufferSize));
        System.out.println(String.format("overlap: %s", overlap));


        File root = new File(datasetRoot);
        List<List<Double>> matrix = new ArrayList<>();
        for (String temperatureFolder : root.list()) {
            if (temperatureFolder.equals("ignored")) {
                continue;
            }
            File directory = new File(root, temperatureFolder);
            int filesProcessed = 0;
            if (directory.isDirectory()) {
                for (String sample : directory.list()) {

                    if (sample.endsWith(".wav")) {
                        if (filesProcessed++ > maxFiles) {
                            break;
                        }
                        System.out.println(sample);
                        File sampleFile = new File(directory, sample);
                        List<Double> features = FeatureExtractor.extract(sampleFile, sampleRate, bufferSize, overlap);
                        features.add(Double.valueOf(temperatureFolder));
                        matrix.add(features);
                    }
                }
            }
        }

        writeToCSV(matrix);
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
