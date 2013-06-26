package com.example.simple_player;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;

public class MyActivity extends Activity {

    private final String TAG = "Player";
    private ArrayList<String> playList;
    private String foramt = "mp3";
    private ListView listAudio;
    private MediaPlayer mp;
    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
           mp = new MediaPlayer();
           playList = new ArrayList<String>();
           listAudio =(ListView) findViewById(R.id.listAudio);
           final File exStorage =  android.os.Environment.getExternalStorageDirectory();
           Log.d(TAG , "Create exStorage");
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MyActivity.this , R.layout.list_item, R.id.tvName , playList);
                listAudio.setAdapter(adapter);

                listAudio.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                       TextView t  = (TextView) view.findViewById(R.id.tvName);
                       play(t.getText().toString());
                    }
                });
            }
        };
           Thread t = new Thread(new Runnable() {
             @Override
              public void run() {
                search(exStorage);
                handler.sendEmptyMessage(0);
             }
          })  ;
          Log.d(TAG , "Run Tread");
          t.start();




    }

    public void search(File file){

       if(file.isDirectory() ){
           Log.d(TAG , "File is Directory");
           File [] list = file.listFiles();
           if(list == null)
               return;
           for (File item:list){
               search(item);
           }
       } else{
           /*
           Узнаем формат
            */
           String formatFile = file.getName();
           formatFile = formatFile.substring(formatFile.length() - 3);
           Log.d(TAG , formatFile);
           /*
           Если формат подходит забрасываем в список
            */
           if(formatFile.equals(foramt)){
               playList.add(file.getPath());
           }
       }
    }
    public void play(String file){
        mp.stop();

                 mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mp.setDataSource(file);
        } catch (IOException e) {
            Log.d(TAG , "Good");
        }

        mp.start();
    }
    }

