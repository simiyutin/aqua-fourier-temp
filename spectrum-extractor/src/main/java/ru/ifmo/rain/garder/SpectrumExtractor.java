package ru.ifmo.rain.garder;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class SpectrumExtractor extends JFrame implements PitchDetectionHandler {
    private int sampleRate = 44100;
    private int bufferSize = 1024 * 4;
    private int overlap = 768 * 4;

    SpectrogramPanel panel = new SpectrogramPanel();

    public SpectrumExtractor(int sampleRate, int bufferSize, int overlap) {
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Spectrogram");
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.overlap = overlap;

        JPanel otherContainer = new JPanel(new BorderLayout());
        otherContainer.add(panel,BorderLayout.CENTER);
        otherContainer.setBorder(new TitledBorder("3. Utter a sound (whistling works best)"));

        this.add(otherContainer,BorderLayout.CENTER);
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

            panel.drawFFT(pitch, amplitudes,fft);
            panel.repaint();

            return true;
        }
    };


    public static void main(final String[] args) throws InterruptedException,
            InvocationTargetException {
        if (args.length != 4) {
            System.out.println("Mindless Self Indulgence - Stupid MF");
            return;
        }

        final String filename = args[0];
        final int sampleRate = Integer.parseInt(args[1]);
        final int bufferSize = Integer.parseInt(args[2]);
        final int overlap = Integer.parseInt(args[3]);

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    // ignore failure to set default look en feel;
                }
                SpectrumExtractor frame = new SpectrumExtractor(sampleRate, bufferSize, overlap);
                frame.pack();
                frame.setSize(750, 750);
                frame.setVisible(true);
                new Thread(() -> {
                    try {
                        frame.getRawData(new File(filename));
                    } catch (Exception e) {
                        throw new AssertionError(e);
                    }
                }).start();
            }
        });
    }
}