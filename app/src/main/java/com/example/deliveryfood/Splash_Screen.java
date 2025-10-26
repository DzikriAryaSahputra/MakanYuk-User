package com.example.deliveryfood;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Timer;
import java.util.TimerTask;

public class Splash_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Timer untuk delay (3 detik)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // Pindah ke MainActivity
                Intent intent = new Intent(Splash_Screen.this, LoginActivity.class);
                startActivity(intent);
                finish(); // supaya splash tidak bisa di-back
            }
        }, 3000); // 3000 ms = 3 detik
    }
}
