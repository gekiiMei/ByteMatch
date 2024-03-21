package com.grptwo.bytematch;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private BackgroundMusicService backgroundMusicService;
    private boolean isServiceBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundMusicService.LocalBinder binder = (BackgroundMusicService.LocalBinder) service;
            backgroundMusicService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            backgroundMusicService = null;
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Start and bind the service
        Intent serviceIntent = new Intent(this, BackgroundMusicService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    public void gotoGame(View v) {
        Intent menuToGame = new Intent(MainActivity.this, Game.class);
        startActivity(menuToGame);
    }

    public void gotoScoreDialog (View v){
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.score_dialog);
        SharedPreferences spRead = getApplicationContext().getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE);
        int mins =spRead.getInt("HighMin", 0);
        int secs =spRead.getInt("HighSecs", 0);

        String secsString = secs < 10 ? "0" + String.valueOf(secs) : String.valueOf(secs);
        String timeLeft = String.valueOf(mins) + ":" + secsString;
        TextView txtHighestTime = dialog.findViewById(R.id.txtHighestTime);
        txtHighestTime.setText(timeLeft);
        dialog.show();

        Button mainMenu2 = dialog.findViewById(R.id.btnMainMenu2);
        mainMenu2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void gotoHelpDialog (View v){
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.help_dialog);
        dialog.show();

        Button mainMenu3 = dialog.findViewById(R.id.btnMainMenu3);
        mainMenu3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}