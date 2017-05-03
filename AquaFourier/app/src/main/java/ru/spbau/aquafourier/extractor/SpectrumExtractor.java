package ru.spbau.aquafourier.extractor;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SpectrumExtractor implements PitchDetectionHandler {
    private int sampleRate = 44100;
    private int bufferSize = 1024 * 4;
    private int overlap = 768 * 4;

    public SpectrumExtractor(int sampleRate, int bufferSize, int overlap) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.overlap = overlap;
    }

    private PitchProcessor.PitchEstimationAlgorithm algo;
    private double pitch;

    final List<float[]> amplitudes = new ArrayList<>();
    final List<Double> pitches = new ArrayList<>();

    double[][] getRawData(File audioFile) throws IOException {
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromPipe(audioFile.getAbsolutePath(), sampleRate, bufferSize, overlap);
        algo = PitchProcessor.PitchEstimationAlgorithm.DYNAMIC_WAVELET;
        dispatcher.addAudioProcessor(new PitchProcessor(algo, sampleRate, bufferSize, this));
        dispatcher.addAudioProcessor(fftProcessor);

        dispatcher.run();

        if (pitches.size() != amplitudes.size()) {
            throw new AssertionError("sizes not equal");
        }

        int n = amplitudes.get(0).length;
        double[][] data = new double[amplitudes.size()][n + 1];
        for (int i = 0; i < amplitudes.size(); i++) {
            data[i][0] = pitches.get(i);
            for (int j = 0; j < n; j++) {
                data[i][j + 1] = amplitudes.get(i)[j];
            }
        }
        return data;
    }

    @Override
    public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
        if(pitchDetectionResult.isPitched()){
            pitch = pitchDetectionResult.getPitch();
        } else {
            pitch = -1;
        }
    }

    AudioProcessor fftProcessor = new AudioProcessor() {

        FFT fft = new FFT(bufferSize);
        float[] amplitudes = new float[bufferSize/2];

        @Override
        public void processingFinished() {
            // TODO Auto-generated method stub
        }

        @Override
        public boolean process(AudioEvent audioEvent) {
            float[] audioFloatBuffer = audioEvent.getFloatBuffer();
            float[] transformbuffer = new float[bufferSize*2];
            System.arraycopy(audioFloatBuffer, 0, transformbuffer, 0, audioFloatBuffer.length);
            fft.forwardTransform(transformbuffer);
            fft.modulus(transformbuffer, amplitudes);
            SpectrumExtractor.this.amplitudes.add(amplitudes);
            SpectrumExtractor.this.pitches.add(pitch);
            return true;
        }
    };

//    public static void main(String[] arg) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
//        if (arg.length != 1) {
//            System.out.println("Usage: ./run.sh <filename.wav>");
//            return;
//        }
//        String filename = arg[0];
//        double[][] data = new SpectrumExtractor().getRawData(filename);
//
//        PrintWriter pw = new PrintWriter(filename.substring(0, filename.length() - 4) + "-spectrum.csv");
//        pw.print("pitch");
//        for (int i = 0; i + 1 < data[0].length; i++) {
//            pw.print(",a" + i);
//        }
//        pw.println();
//        for (int i = 0; i < data.length; i++) {
//            for (int j = 0; j < data[i].length; j++) {
//                if (j != 0) {
//                    pw.print(",");
//                }
//                pw.printf("%.3f", data[i][j]);
//            }
//            pw.println();
//        }
//        pw.close();
//    }
}