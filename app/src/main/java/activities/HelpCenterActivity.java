package activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.deliveryfood.R;

public class HelpCenterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);

        ImageView btnBack = findViewById(R.id.btnBackHelp);
        CardView btnWA = findViewById(R.id.btnContactWA);
        CardView btnEmail = findViewById(R.id.btnContactEmail);

        // 1. Tombol Kembali
        btnBack.setOnClickListener(v -> finish());

        // 2. Tombol WhatsApp
        btnWA.setOnClickListener(v -> {
            String phoneNumber = "6281286855764"; // Ganti dengan nomor Admin (Kode negara 62)
            String message = "Halo Admin MakanYuk, saya butuh bantuan terkait pesanan saya.";

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + message));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Aplikasi WhatsApp tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Tombol Email
        btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"help@makanyuk.com"}); // Email tujuan
            intent.putExtra(Intent.EXTRA_SUBJECT, "Bantuan Aplikasi MakanYuk");

            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Tidak ada aplikasi email ditemukan", Toast.LENGTH_SHORT).show();
            }
        });
    }
}