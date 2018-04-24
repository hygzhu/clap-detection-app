package com.hzhudev.clap_detection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import java.util.Random;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;


public class MainActivity extends AppCompatActivity {

    int[] files = {R.raw.meme1, R.raw.meme2,R.raw.meme3,R.raw.meme4,R.raw.meme5};
    MediaPlayer mp = null;
    int claps = 0;

    private static final int REQUEST_AUDIO_PERMISSION_RESULT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startRecording();
    }

    public void startRecording(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED) {
                //Version>=Marshmallow
                startAudioDispatcher();
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(this,
                            "App required access to audio", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO
                }, REQUEST_AUDIO_PERMISSION_RESULT);
            }

        } else {
            //Version < Marshmallow
            startAudioDispatcher();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_AUDIO_PERMISSION_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application will not have audio on record", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startAudioDispatcher(){
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);
        double threshold = 20;
        double sensitivity = 80;
        PercussionOnsetDetector mPercussionDetector = new PercussionOnsetDetector(22050, 1024,
                new OnsetHandler() {
                    @Override
                    public void handleOnset(double time, double salience) {
                        System.out.println("Time: "+ time + " Salience:" + salience);

                        if(claps == 0){
                            claps++;
                            return;
                        }else if(claps == 1){
                            playAudio();
                            claps++;
                        }else{
                            claps = 0;
                        }
                    }
                }, sensitivity, threshold);
        dispatcher.addAudioProcessor(mPercussionDetector);
        new Thread(dispatcher,"Audio Dispatcher").start();
    }

    public void playAudio(){
        int rnd = new Random().nextInt(files.length);

        if(mp == null){
            mp = MediaPlayer.create(this, files[0]);
        }
        if(!mp.isPlaying()){
            mp = MediaPlayer.create(this, files[rnd]);
            mp.start();
        }
    }
}
