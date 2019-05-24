package com.dreamso.radio;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dreamso.radio.player.PlaybackStatus;
import com.dreamso.radio.player.RadioManager;
import com.dreamso.radio.player.VisualizerView;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.playTrigger)
    ImageButton trigger;




    @BindView(R.id.marquee)
    TextView marqueeText;

    @BindView(R.id.fblogo)
    ImageView fbView;

    @BindView(R.id.websitelogo)
    ImageView webView;

    @BindView(R.id.view_visualizer)
    VisualizerView mVisualizerView;

    @BindView(R.id.seekBar)
    SeekBar volumeSeekbar;


    RadioManager radioManager;

    String streamURL = "http://109.236.85.141:7316/";


    //visualiser
    private Visualizer mVisualizer;
    private static final int PERM_REQ_CODE = 23;

    private AudioManager audioManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        marqueeText.setSelected(true);

        radioManager = RadioManager.with(this);


        if(!checkAudioPermission()){
            requestAudioPermission();
        }

        initAudio();

        initControls();


        fbView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Uri uri = Uri.parse("http://www.facebook.com/radiolkonline");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        webView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Uri uri = Uri.parse("http://www.radiolanka.lk/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

    }


    private void initAudio() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setupVisualizerFxAndUi();
        mVisualizer.setEnabled(true);
    }

    private void initControls()
    {
        try
        {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekbar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekbar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onStopTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
                {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }


            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        {
            int index = volumeSeekbar.getProgress();
            volumeSeekbar.setProgress(index + 1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        {
            int index = volumeSeekbar.getProgress();
            volumeSeekbar.setProgress(index - 1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void setupVisualizerFxAndUi() {
        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(0); // Using system audio session ID
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(
                            Visualizer visualizer,
                            byte[] bytes,
                            int samplingRate) {
                        mVisualizerView.updateVisualizer(bytes);
                    }

                    public void onFftDataCapture(
                            Visualizer visualizer,
                            byte[] bytes,
                            int samplingRate) {
                        // Do nothing for now
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }



    @Override
    public void onStart() {

        super.onStart();

        EventBus.getDefault().register(this);


    }


    @Override
    public void onBackPressed() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        //unBoundAudioService();
                        //finish();
                        radioManager.unbind();
                        MainActivity.super.onBackPressed();



                    }
                }).create();

        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FF7800"));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FF7800"));
            }
        });


        dialog.show();
    }

    private boolean checkAudioPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERM_REQ_CODE);
    }

    @Override
    public void onStop() {

        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        radioManager.unbind();

        super.onDestroy();

        if (mVisualizer == null) {
            return;
        }
        mVisualizer.setEnabled(false);
        mVisualizer.release();
    }

    @Override
    protected void onResume() {
        super.onResume();

        radioManager.bind();
    }



    @Subscribe
    public void onEvent(String status){

        switch (status){

            case PlaybackStatus.LOADING:

                // loading

                break;

            case PlaybackStatus.ERROR:

                Toast.makeText(this, R.string.no_stream, Toast.LENGTH_SHORT).show();

                break;

        }

        trigger.setImageResource(status.equals(PlaybackStatus.PLAYING)
                ? R.drawable.pausebtnnew
                : R.drawable.playbtnnew);

    }

    @OnClick(R.id.playTrigger)
    public void onClicked(){

        if(TextUtils.isEmpty(streamURL)) return;

        radioManager.playOrPause(streamURL);
    }


}
