package ru.spbau.aquafourier;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.spbau.aquafourier.extractor.FeatureExtractor;
import simplesound.pcm.PcmAudioHelper;
import simplesound.pcm.WavAudioFormat;

import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int SAMPLING_RATE = 44100;
    private static final long RECORD_TIME_MILLIS = TimeUnit.SECONDS.toMillis(3);
    private static final String SERVER_URL = "http://91.121.160.193:5000/";

    private Button measureButton;
    private TextView temperatureText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        temperatureText = (TextView) findViewById(R.id.temperatureText);
        measureButton = (Button) findViewById(R.id.measureButton);
        measureButton.setOnClickListener(this);

        new AndroidFFMPEGLocator(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.measureButton) {
            throw new AssertionError("Unknown click event");
        }

        measureButton.setEnabled(false);
        temperatureText.setText(R.string.recording);
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                int bufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                String filenameRaw = getExternalCacheDir().getAbsolutePath() + "/water.raw";
                String filenameWav = getExternalCacheDir().getAbsolutePath() + "/water.wav";

                try {
                    FileOutputStream outRaw = new FileOutputStream(filenameRaw);
                    byte[] buffer = new byte[bufferSize];
                    long startTime = System.currentTimeMillis();
                    record.startRecording();
                    int totalRead = 0;
                    while (System.currentTimeMillis() - startTime < RECORD_TIME_MILLIS) {
                        int read = record.read(buffer, 0, bufferSize);
                        if ((read == AudioRecord.ERROR_INVALID_OPERATION) ||
                                (read == AudioRecord.ERROR_BAD_VALUE) ||
                                (read <= 0)) {
                            continue;
                        }
                        totalRead += read;
                        outRaw.write(buffer, 0, read);
                    }
                    outRaw.close();
                    PcmAudioHelper.convertRawToWav(WavAudioFormat.mono16Bit(SAMPLING_RATE),
                            new File(filenameRaw), new File(filenameWav));
                } catch (IOException e) {
                    throw new AssertionError(e);
                }

                return filenameWav;
            }

            @Override
            protected void onPostExecute(String filename) {
                prepareData(filename);
            }
        }.execute();
    }

    private void prepareData(final String filename) {
        temperatureText.setText(R.string.processing);
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                List<Double> data;
                try {
                    data = FeatureExtractor.extract(new File(filename));
                } catch (Exception e) {
                    throw new AssertionError(e);
                }

                Log.i(LOG_TAG, "data.size() = " + data.size());

                StringBuilder converted = new StringBuilder();
                for (int i = 0; i < data.size(); i++) {
                    if (i != 0) {
                        converted.append(" ");
                    }
                    converted.append(data.get(i));
                }

                return converted.toString();
            }

            @Override
            protected void onPostExecute(String s) {
//                sendRequest(s);
                sendRequest("1 2 3");
            }
        }.execute();
    }

    private void sendRequest(final String data) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                URL url;
                try {
                    url = new URL(SERVER_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "text/plain");
                    connection.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    OutputStream os = connection.getOutputStream();
                    os.write(data.getBytes());
                    os.close();

                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String response = br.readLine();
                    return response;
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }

            @Override
            protected void onPostExecute(String response) {
                showTemperature(response);
            }
        }.execute();
    }

    private void showTemperature(String temperature) {
        temperatureText.setText(temperature);
//        temperatureText.setText(String.format(Locale.US, "%.1f°C", temperature));
        measureButton.setEnabled(true);
    }
}
