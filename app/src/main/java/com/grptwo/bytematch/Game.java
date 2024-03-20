package com.grptwo.bytematch;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class Game extends AppCompatActivity {
    //game vars
    int points = 0;
    CountDownTimer gameTimer = null;
    long endTime = 0L;
    int[][] cardValues = {
            {-1, -1, -1},
            {-1, -1, -1},
            {-1, -1, -1},
            {-1, -1, -1}
    };
    ImageButton[][] cardComps = new ImageButton[4][3];
    int[] card1Coords = {-1, -1};
    int[] card2Coords = {-1, -1};
    boolean hasOpen = false;
    boolean waiting = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //initialize game
        readyDialog();
    }

    public void readyDialog() {
        Dialog dialog = new Dialog(Game.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.ready_dialog);
        Button startButton = dialog.findViewById(R.id.btnStartGame);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //star timer
                startTimer();
                dialog.dismiss();
            }
        });
        initializeGame();
        dialog.show();
    }

    public void winDialog() {
        Dialog dialog = new Dialog(Game.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.win_msg);
        Button mainMenu = dialog.findViewById(R.id.btnMainMenu);
        int mins = Math.toIntExact(endTime/1000/60);
        int secs = Math.toIntExact(endTime/1000);
        String secsString = secs < 10 ? "0" + String.valueOf(secs) : String.valueOf(secs);
        String timeLeft = String.valueOf(mins) + ":" + secsString;
        TextView finTime = dialog.findViewById(R.id.txtFinTime);
        finTime.setText(timeLeft);

        mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameToMain = new Intent(Game.this, MainActivity.class);
                dialog.dismiss();
                startActivity(gameToMain);
            }
        });
        dialog.show();
    }

    public void loseDialog() {
        Dialog dialog = new Dialog(Game.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.lose_msg);
        Button mainMenu = dialog.findViewById(R.id.btnLoseMainMenu);
        Button tryAgain = dialog.findViewById(R.id.btnTryAgain);
        mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameToMain = new Intent(Game.this, MainActivity.class);
                dialog.dismiss();
                startActivity(gameToMain);
            }
        });
        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
            }
        });
        dialog.show();
    }

    public void initializeGame() {
        loadComps();
        Random r = new Random();
        int n = 0;
        for (int i=0; i<4; ++i) {
            for (int j=0; j<3; ++j) {
                do {
                    n = r.nextInt(6);
                    Log.d("loop", String.valueOf(n));
                } while (countOccur(cardValues, n)>=2);
                Log.d("loop", "Settled on" + String.valueOf(n));
                cardValues[i][j] = n;
            }
        }
        points = 0;
    }

    public void startTimer() {
        gameTimer = new CountDownTimer(60000, 1000) {
            public void onFinish() {
                //lose
                Log.d("gameEnd", "Lose!");
                loseDialog();
            }
            public void onTick(long millisUntilFinished) {
                int mins = Math.toIntExact(millisUntilFinished/1000/60);
                int secs = Math.toIntExact(millisUntilFinished/1000);
                String secsString = secs < 10 ? "0" + String.valueOf(secs) : String.valueOf(secs);
                String timeLeft = String.valueOf(mins) + ":" + secsString;
                TextView timer = findViewById(R.id.txtTimer);
                endTime = millisUntilFinished;
                timer.setText(timeLeft);
            }
        }.start();
    }

    public void cardClick(View v) {
        if (waiting) {
            return;
        }
        String[] coords = v.getTag().toString().split("_");
        int currX = Integer.parseInt(coords[1]);
        int currY = Integer.parseInt(coords[0]);
        Log.d("coords", "X: " + String.valueOf(currX) + " Y: " + String.valueOf(currY));
        Log.d("value", "Value: " + String.valueOf(cardValues[currY][currX]));
        if (cardValues[currY][currX] == -1) {
            return;
        }
        if (!hasOpen) {
            card1Coords[0] = currY;
            card1Coords[1] = currX;
            flipOpen(currY, currX);
            hasOpen = true;
        } else {
            if (currY == card1Coords[0] && currX == card1Coords[1]) {
                return;
            }
            card2Coords[0] = currY;
            card2Coords[1] = currX;
            flipOpen(currY, currX);
            waiting = true;
            new CountDownTimer(1000, 1000) {
                public void onFinish() {
                    waiting = false;
                    evaluateCards();
                }
                public void onTick(long millisUntilFinished) {}
            }.start();
            hasOpen = false;
        }
    }

    public void evaluateCards() {
        Log.d("eval", "Evaluating..");
        int y1 = card1Coords[0], x1 = card1Coords[1];
        int y2 = card2Coords[0], x2 = card2Coords[1];
        Log.d("evalCoords", "y1: " + y1 + " x1: " + x1);
        Log.d("evalCoords", "y2: " + y2 + " x2: " + x2);
        card1Coords[0] = card1Coords[1] = -1;
        card2Coords[0] = card2Coords[1] = -1;
        int card1Val = cardValues[y1][x1];
        int card2Val = cardValues[y2][x2];

        if (card1Val == card2Val) {
            points++;
            cardValues[y1][x1] = -1;
            cardValues[y2][x2] = -1;
            if (points == 6) {
                //win
                Log.d("gameEnd", "Win!");
                gameTimer.cancel();
                Log.d("winTime", "Milliseconds left: " + endTime);
                winDialog();
            }
        } else {
            flipClose(y1, x1);
            flipClose(y2, x2);
        }
    }

    public void flipOpen(int y, int x) {
        int[]  cardFaces= {
                R.drawable.card_headphones,
                R.drawable.card_laptop,
                R.drawable.card_monitor,
                R.drawable.card_printer,
                R.drawable.card_smartwatch,
                R.drawable.card_vr
        };
        cardComps[y][x].setImageResource(cardFaces[cardValues[y][x]]);
    }

    public void flipClose(int y, int x) {
        cardComps[y][x].setImageResource(R.drawable.card_back);
    }

    ///helpers
    public void loadComps() {
        cardComps[0][0] = findViewById(R.id.card0_0);
        cardComps[0][1] = findViewById(R.id.card0_1);
        cardComps[0][2] = findViewById(R.id.card0_2);
        cardComps[1][0] = findViewById(R.id.card1_0);
        cardComps[1][1] = findViewById(R.id.card1_1);
        cardComps[1][2] = findViewById(R.id.card1_2);
        cardComps[2][0] = findViewById(R.id.card2_0);
        cardComps[2][1] = findViewById(R.id.card2_1);
        cardComps[2][2] = findViewById(R.id.card2_2);
        cardComps[3][0] = findViewById(R.id.card3_0);
        cardComps[3][1] = findViewById(R.id.card3_1);
        cardComps[3][2] = findViewById(R.id.card3_2);
    }

    public int countOccur(int[][] arr, int t) {
        int r = 0;
        for (int i=0; i<arr.length; ++i) {
            for (int j=0; j<arr[0].length; ++j) {
                if (arr[i][j] == t) {
                    r++;
                }
            }
        }
        return r;
    }
}