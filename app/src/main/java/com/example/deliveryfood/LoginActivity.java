package com.example.deliveryfood;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // --- VARIABEL BARU ---
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private FirebaseAuth mAuth;
    // --------------------

    // Variabel Anda yang sudah ada
    private TextView textPindahKeDaftar;
    private TextView textPindahKeForget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Boilerplate
        setContentView(R.layout.activity_login);

        // --- INISIALISASI BARU ---
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.editTextTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextTextPassword);
        buttonLogin = findViewById(R.id.button);
        // -----------------------

        // Inisialisasi dari kode Anda yang sudah ada
        textPindahKeDaftar = findViewById(R.id.textView3);
        textPindahKeForget = findViewById(R.id.textView2);

        // Boilerplate
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- ONCLICK LISTENER BARU UNTUK TOMBOL LOGIN ---
        buttonLogin.setOnClickListener(v -> loginUser());
        // ---------------------------------------------

        // Listener Anda yang sudah ada untuk pindah halaman
        textPindahKeDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignActivity.class);
                startActivity(intent);
            }
        });

        textPindahKeForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pastikan nama Activity Anda adalah ForgotPasswordActivity.class
                Intent intent = new Intent(LoginActivity.this, ForgetActivity.class);
                startActivity(intent);
            }
        });
    }

    // --- FUNGSI BARU UNTUK LOGIKA LOGIN ---
    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validasi input
        if (email.isEmpty()) {
            editTextEmail.setError("Email tidak boleh kosong");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Format email tidak valid");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Password tidak boleh kosong");
            editTextPassword.requestFocus();
            return;
        }

        // Proses Login ke Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login berhasil
                            Toast.makeText(LoginActivity.this, "Login Berhasil.", Toast.LENGTH_SHORT).show();
                            // Arahkan ke halaman utama/dashboard aplikasi
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            // Membersihkan activity stack agar tidak bisa kembali ke halaman login
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Login gagal
                            Toast.makeText(LoginActivity.this, "Login Gagal: Email atau Password salah.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}