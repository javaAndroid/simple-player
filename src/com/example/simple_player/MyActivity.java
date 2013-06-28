package com.example.simple_player;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.*;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MyActivity extends Activity implements View.OnClickListener {

    private final String TAG = "Player";
    private ArrayList<String> playList;
    private String foramt = "mp3";
    private ListView listAudio;
    private MediaPlayer mp;
    private Button btnPlay, btnPrev, btnNext;
    private int size, position;
    private ProgressDialog prgDialog;
    private SeekBar prgAudio;
    private Timer time;
    private TextView timeOfPlay , timeAudio;
    private SeekBarHandler handler = new SeekBarHandler();
    private Handler handler112 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            timeAudio.setText(msg.getData().getString("time"));
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        timeOfPlay = (TextView) findViewById(R.id.timeOfPlay);
        timeAudio = (TextView) findViewById(R.id.timeAudio);

        prgAudio = (SeekBar) findViewById(R.id.prgAudio);
        prgDialog = new ProgressDialog(this);
        prgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prgDialog.show();
        mp = new MediaPlayer();
        playList = new ArrayList<String>();
        listAudio = (ListView) findViewById(R.id.listAudio);
        final File exStorage = android.os.Environment.getExternalStorageDirectory();
        Log.d(TAG, "Create exStorage");
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MyActivity.this, android.R.layout.simple_expandable_list_item_1, android.R.id.text1, playList);
                listAudio.setAdapter(adapter);

                listAudio.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        MyActivity.this.position = position;
                        TextView t = (TextView) view.findViewById(android.R.id.text1);
                        Log.d(TAG, t.getText().toString());
                        play(t.getText().toString());
                    }
                });
            }
        };
        final Handler handler1 = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                prgDialog.hide();
            }
        };
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                search(exStorage);
                handler.sendEmptyMessage(0);
                handler1.sendEmptyMessage(0);
            }
        });
        Log.d(TAG, "Run Tread ");
        t.start();


        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(this);

    }

    public void search(File file) {

        if (file.isDirectory()) {
            Log.d(TAG, "File is Directory");
            File[] list = file.listFiles();
            if (list == null)
                return;
            for (File item : list) {
                search(item);
            }
        } else {
           /*
           Узнаем формат
            */
            String formatFile = file.getName();
            formatFile = formatFile.substring(formatFile.length() - 3);
            Log.d(TAG, formatFile);
           /*
           Если формат подходит забрасываем в список
            */
            if (formatFile.equals(foramt)) {
                ++size;
                playList.add(file.getPath());
            }
        }
    }

    public void play(String file) {

        if (mp.isPlaying())
            mp.stop();

        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        btnPlay.setText(getResources().getString(R.string.pause));
        try {
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    final byte minutes = (byte) ((mp.getDuration()/1000)/60);
                    final byte seconds = (byte) ((mp.getDuration()/1000)%60);

                    Message msg = new Message();
                    Bundle b = new Bundle();
                    b.putString("time" , minutes+":"+seconds);
                    msg.setData(b);
                    handler112.sendMessage(msg);
                }
            });

            mp.setDataSource(file);
            mp.prepare();
            mp.start();

            prgAudio.setMax(mp.getDuration() / 1000);


            if (time == null) {
                time = new Timer(prgAudio.getMax(), handler);
                time.start();
            } else {
                time.setStop(true);
                time = new Timer(prgAudio.getMax(), handler);
                time.start();
            }
        } catch (IOException e) {
            Log.d(TAG, "Good");
        }
    }

    @Override
    public void onClick(View v) {
       if(v.getId() == btnPlay.getId()){
           if (mp.isPlaying()) {
               mp.pause();
               btnPlay.setText(getResources().getString(R.string.play));
               if (time != null) {
                   time.setPause(true);
               }
           } else {
               mp.start();
               btnPlay.setText(getResources().getString(R.string.pause));
               if (time != null) {
                   time.setPause(false);
               }
           }
       }
    }



    // Classes

    class Timer extends Thread {
        private int time;
        private Handler handler;
        private boolean stop;
        private boolean pause;

        Timer(int time, Handler handler) {
            this.time = time;
            this.handler = handler;
        }

        public void setStop(boolean value) {
            stop = value;
        }

        public boolean getStop() {
            return stop;
        }

        public void setPause(boolean value) {
            pause = value;
        }

        public boolean getPause() {
            return pause;
        }

        @Override
        public void run() {
            int second = 0;

            while (second <= time && !stop) {
                if (pause)
                    continue;
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {

                }
                ++second;
                handler.sendEmptyMessage(second);
            }
        }
    }
    class SeekBarHandler extends Handler {
        byte minutes;
        byte seconds;
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            prgAudio.setProgress(msg.what);
             minutes = (byte) (msg.what/60);
            seconds = (byte) (msg.what%60);
            String time = new String();
            if(minutes < 10)
                time += 0;
             time+= minutes;
             time+=':';
              if(seconds < 10)
                  time+=0;
            time+=seconds;
            timeOfPlay.setText(time);
        }
    }

}

