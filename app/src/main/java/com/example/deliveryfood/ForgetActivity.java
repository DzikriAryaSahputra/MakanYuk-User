package com.example.deliveryfood;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

// Nama kelas harus sama dengan nama file: ForgetActivity
public class ForgetActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private Button buttonSendLink;
    private ImageButton backButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pastikan nama file layout Anda adalah activity_forget.xml
        setContentView(R.layout.activity_forget);

        // Sesuaikan ID dengan yang ada di file activity_forget.xml
        editTextEmail = findViewById(R.id.editTextEmailForgot);
        buttonSendLink = findViewById(R.id.buttonSendLink);
        mAuth = FirebaseAuth.getInstance();
        backButton = findViewById(R.id.backButton);
        setupButtonListeners();

        buttonSendLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResetLink();
            }
        });
    }
    private void setupButtonListeners() {
        // Tombol kembali
        backButton.setOnClickListener(v -> {
            onBackPressed(); // Fungsi bawaan untuk kembali
        });
    }
    private void sendResetLink() {
        String email = editTextEmail.getText().toString().trim();

        // Validasi input email
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Masukkan email yang valid");
            editTextEmail.requestFocus();
            return;
        }

        // Kirim email reset password menggunakan Firebase
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgetActivity.this, "Link reset password telah dikirim ke email Anda.", Toast.LENGTH_LONG).show();
                        } else {
                            // PERBAIKAN: Gunakan nama kelas yang benar yaitu ForgetActivity.this
                            Toast.makeText(ForgetActivity.this, "Gagal mengirim link. Pastikan email terdaftar.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

}