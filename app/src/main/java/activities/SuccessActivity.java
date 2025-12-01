package activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
// --- TAMBAHKAN IMPORT INI ---
import androidx.activity.OnBackPressedCallback;

import com.example.deliveryfood.R;

public class SuccessActivity extends AppCompatActivity {

    private Button buttonOK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        buttonOK = findViewById(R.id.buttonOK);
        buttonOK.setOnClickListener(v -> {
            goToMainActivity();
        });

        // --- GANTI LOGIKA onBackPressed() DENGAN INI ---
        // 1. Buat sebuah callback baru
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // 2. Tidak melakukan apa-apa di sini
                // Ini akan memblokir tombol kembali (baik fisik maupun gesture)
            }
        };
        // 3. Tambahkan callback ke dispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);
        // -----------------------------------------------
    }

    // Buat fungsi bantu untuk pindah
    private void goToMainActivity() {
        Intent intent = new Intent(SuccessActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Tutup activity ini
    }

}