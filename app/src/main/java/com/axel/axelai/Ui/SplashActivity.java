package com.axel.axelai.Ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;// Make sure to import your MainActivity
import com.axel.axelai.R;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0; // Initialize progress status
    private Handler handler = new Handler(); // To handle the progress bar update

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen); // Ensure this matches your XML file name

        // Initialize the ProgressBar
        progressBar = findViewById(R.id.progressBar);

        // Simulate loading in a new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progressStatus < 100) {
                    // Update the progress status
                    progressStatus += 1;

                    // Update the progress bar
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });

                    // Sleep for a while to simulate loading (for example, 50ms)
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // After loading is complete, start the MainActivity
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Finish the splash activity so the user can't return to it
            }
        }).start();
    }
}
