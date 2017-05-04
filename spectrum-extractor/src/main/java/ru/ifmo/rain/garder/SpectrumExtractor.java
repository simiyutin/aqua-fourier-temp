package ru.ifmo.rain.garder;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
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

    double[][] getRawData(File audioFile) throws IOException, UnsupportedAudioFileException, LineUnavailableException {

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(audioFile, bufferSize, overlap);
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
}