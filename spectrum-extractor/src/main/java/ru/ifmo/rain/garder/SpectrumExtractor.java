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
    private float sampleRate = 44100;
    private int bufferSize = 1024 * 4;
    private int overlap = 768 * 4;

    private PitchProcessor.PitchEstimationAlgorithm algo;
    private double pitch;

    final List<float[]> amplitudes = new ArrayList<>();
    final List<Double> pitches = new ArrayList<>();

    void run(String filename) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        File audioFile = new File(filename);
        PrintWriter pw = new PrintWriter(filename.substring(0, filename.length() - 4) + "-spectrum.csv");

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(audioFile, bufferSize, overlap);
        AudioFormat format = AudioSystem.getAudioFileFormat(audioFile).getFormat();
        dispatcher.addAudioProcessor(new AudioPlayer(format));
        algo = PitchProcessor.PitchEstimationAlgorithm.DYNAMIC_WAVELET;
        dispatcher.addAudioProcessor(new PitchProcessor(algo, sampleRate, bufferSize, this));
        dispatcher.addAudioProcessor(fftProcessor);

        dispatcher.run();

        if (pitches.size() != amplitudes.size()) {
            throw new AssertionError("sizes not equal");
        }

        int n = amplitudes.get(0).length;
        pw.print("pitch");
        for (int i = 0; i < n; i++) {
            pw.print(",a" + i);
        }
        pw.println();
        for (int i = 0; i < amplitudes.size(); i++) {
            pw.printf("%.3f", pitches.get(i));
            for (float a : amplitudes.get(i)) {
                pw.printf(",%.3f", a);
            }
            pw.println();
        }
        pw.close();
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

    public static void main(String[] arg) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (arg.length != 1) {
            System.out.println("Usage: java -jar ru.ifmo.rain.garder.SpectrumExtractor.jar <filename.wav>");
            return;
        }
        new SpectrumExtractor().run(arg[0]);
    }
}