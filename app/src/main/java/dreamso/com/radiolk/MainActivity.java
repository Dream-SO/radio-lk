package dreamso.com.radiolk;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private AudioServiceBinder audioServiceBinder = null;

    private Handler audioProgressUpdateHandler = null;
    private static final int PERM_REQ_CODE = 23;
    int audioSessionId = -1;
    BarVisualizer mVisualizer;
    ImageView fbView;
    ImageView webView;
    private TextView tv;
    private ProgressDialog progressDialog;
    final String audioFileUrl = "http://109.236.85.141:7316/;";
    public boolean isSetaudioPlayer = false;




    private TextView audioFileUrlTextView;

    // This service connection object is the bridge between activity and background service.
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // Cast and assign background service's onBind method returned iBander object.
            audioServiceBinder = (AudioServiceBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVisualizer = findViewById(R.id.blast);
        tv = (TextView) this.findViewById(R.id.marquee);
        tv.setSelected(true);  // Set focus to the textview

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");



        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(Color.parseColor("#FF7800")));

        if(!checkAudioPermission()){
            requestAudioPermission();
        }

        fbView = (ImageView) findViewById(R.id.fblogo);
        fbView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Uri uri = Uri.parse("http://www.facebook.com/radiolkonline");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        webView = (ImageView) findViewById(R.id.websitelogo);
        webView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Uri uri = Uri.parse("http://www.radiolanka.lk/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        // Bind background audio service when activity is created.
        bindAudioService();




        //final String audioFileUrl = "http://www.dev2qa.com/demo/media/test.mp3";


     //   backgroundAudioProgress = (ProgressBar)findViewById(R.id.play_audio_in_background_service_progressbar);

        // Get audio file url textview.
        audioFileUrlTextView = (TextView)findViewById(R.id.audio_file_url_text_view);
        if(audioFileUrlTextView != null)
        {
            // Show web audio file url in the text view.
            //audioFileUrlTextView.setText("Audio File Url. \r\n" + audioFileUrl);


        }

        // Click this button to start play audio in a background service.
        Button startBackgroundAudio = (Button)findViewById(R.id.start_audio_in_background);
        startBackgroundAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                //Toast.makeText(getApplicationContext(), "starting.. RadioLK", Toast.LENGTH_LONG).show();
                // Set web audio file url
                audioServiceBinder.setAudioFileUrl(audioFileUrl);



                // Web audio is a stream audio.
                audioServiceBinder.setStreamAudio(true);

                // Set application context.
                audioServiceBinder.setContext(getApplicationContext());

                // Initialize audio progress bar updater Handler object.
                createAudioProgressbarUpdater();
                audioServiceBinder.setAudioProgressUpdateHandler(audioProgressUpdateHandler);

                // Start audio in background service.
                audioServiceBinder.startAudio();

                audioSessionId = audioServiceBinder.getAudioSessionId();

                if (audioSessionId != -1 && audioSessionId != AudioManager.ERROR){
                    mVisualizer.setAudioSessionId(audioSessionId);
                }

               // backgroundAudioProgress.setVisibility(ProgressBar.VISIBLE);
                if (progressDialog.isShowing()) {
                    progressDialog.cancel();
                }

                //Toast.makeText(getApplicationContext(), "playing RadioLK", Toast.LENGTH_LONG).show();
            }
        });

        // Click this button to pause the audio played in background service.
        Button pauseBackgroundAudio = (Button)findViewById(R.id.pause_audio_in_background);
        pauseBackgroundAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioServiceBinder.pauseAudio();
                //Toast.makeText(getApplicationContext(), "Play web audio file is paused.", Toast.LENGTH_LONG).show();
            }
        });

        // Click this button to stop the media player in background service.
        Button stopBackgroundAudio = (Button)findViewById(R.id.stop_audio_in_background);
        stopBackgroundAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioServiceBinder.stopAudio();
               // backgroundAudioProgress.setVisibility(ProgressBar.INVISIBLE);
                //Toast.makeText(getApplicationContext(), "Stop play web audio file.", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void setPlayer(){
        // Set web audio file url
        audioServiceBinder.setAudioFileUrl(audioFileUrl);



        // Web audio is a stream audio.
        audioServiceBinder.setStreamAudio(true);

        // Set application context.
        audioServiceBinder.setContext(getApplicationContext());

        // Initialize audio progress bar updater Handler object.
        createAudioProgressbarUpdater();
        audioServiceBinder.setAudioProgressUpdateHandler(audioProgressUpdateHandler);

        audioSessionId = audioServiceBinder.getAudioSessionId();

        if (audioSessionId != -1 && audioSessionId != AudioManager.ERROR){
            mVisualizer.setAudioSessionId(audioSessionId);
        }

        isSetaudioPlayer = true;

        // Start audio in background service.
        //audioServiceBinder.startAudio();



    }



    private boolean checkAudioPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERM_REQ_CODE);
    }

    // Bind background service with caller activity. Then this activity can use
    // background service's AudioServiceBinder instance to invoke related methods.
    private void bindAudioService()
    {
        if(audioServiceBinder == null) {
            Intent intent = new Intent(MainActivity.this, AudioService.class);

            // Below code will invoke serviceConnection's onServiceConnected method.
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    // Unbound background audio service with caller activity.
    private void unBoundAudioService()
    {
        if(audioServiceBinder != null) {
            unbindService(serviceConnection);
        }
    }

    @Override
    protected void onDestroy() {
        // Unbound background audio service when activity is destroyed.
        unBoundAudioService();
        super.onDestroy();
    }

    // Create audio player progressbar updater.
    // This updater is used to update progressbar to reflect audio play process.
    private void createAudioProgressbarUpdater() {
        /* Initialize audio progress handler. */
        if (audioProgressUpdateHandler == null) {
            audioProgressUpdateHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    // The update process message is sent from AudioServiceBinder class's thread object.
                    if (msg.what == audioServiceBinder.UPDATE_AUDIO_PROGRESS_BAR) {

                        if (audioServiceBinder != null) {
                            // Calculate the percentage.
                            int currProgress = audioServiceBinder.getAudioProgress();

                            // Update progressbar. Make the value 10 times to show more clear UI change.
                           // backgroundAudioProgress.setProgress(currProgress * 10);
                        }
                    }
                }
            };
        }
    }
}
