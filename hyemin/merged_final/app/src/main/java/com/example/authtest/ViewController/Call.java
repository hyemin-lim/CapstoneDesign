package com.example.authtest.ViewController;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.authtest.R;

public class Call extends AppCompatActivity {

    ImageButton button1;
    ImageButton button2;

    MediaPlayer mAudio = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);


        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        PlayTest();


        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);

        button1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                StopTest();

                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                StopTest();

                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });

    }

    public void PlayTest(){
        try{
            mAudio = new MediaPlayer();
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mAudio.setDataSource(this, alert);

            mAudio.setAudioStreamType(AudioManager.STREAM_RING);

            mAudio.setLooping(true);
            mAudio.prepare();
        }
        catch(Exception e){

        }

        mAudio.start();
    }

    public void StopTest(){
        if(mAudio.isPlaying())
        {
            mAudio.stop();
        }
        else
        {

        }
    }
}