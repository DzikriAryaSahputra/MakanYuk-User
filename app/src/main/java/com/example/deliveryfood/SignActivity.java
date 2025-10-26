package com.example.deliveryfood;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar; // Import ProgressBar
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Import Firebase
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignActivity extends AppCompatActivity {

    // Deklarasi variabel untuk komponen UI dan Firebase
    EditText etUsername, etEmail, etPhone, etPassword;
    Button buttonSignUp;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    // Tambahkan ProgressBar untuk feedback visual saat loading
    private ProgressBar progressBar; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign);

        // Inisialisasi Firebase Auth dan Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Hubungkan variabel dengan ID yang benar dari XML Anda
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.editTextTextEmailAddress2);
        etPhone = findViewById(R.id.editTextPhone);
        etPassword = findViewById(R.id.editTextTextPassword2);
        buttonSignUp = findViewById(R.id.button2);
        // Hubungkan ProgressBar, asumsikan Anda menambahkannya di XML dengan id progressBar
        // progressBar = findViewById(R.id.progressBar); 

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set listener untuk tombol sign up
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        // Ambil data dari form
        String name = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // --- Validasi Input ---
        if (name.isEmpty()) {
            etUsername.setError("Nama tidak boleh kosong");
            etUsername.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email tidak boleh kosong");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format email tidak valid");
            etEmail.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Nomor handphone tidak boleh kosong");
            etPhone.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password tidak boleh kosong");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 8) {
            etPassword.setError("Password minimal harus 8 karakter");
            etPassword.requestFocus();
            return;
        }

        // Tampilkan loading dan nonaktifkan tombol untuk mencegah klik ganda
        // progressBar.setVisibility(View.VISIBLE);
        buttonSignUp.setEnabled(false);

        // --- Proses Pendaftaran ke Firebase ---
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Langkah 1: Pendaftaran di Authentication BERHASIL
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String userId = firebaseUser.getUid();

                            // Langkah 2: Siapkan dan Simpan data tambahan ke Firestore
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", name);
                            user.put("email", email);
                            user.put("phone", phone);
                            user.put("role", "pelanggan"); // Menambahkan peran default

                            db.collection("users").document(userId)
                                    .set(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // Sembunyikan loading
                                            // progressBar.setVisibility(View.GONE);
                                            
                                            if (task.isSuccessful()) {
                                                // Jika Auth & Firestore berhasil, tampilkan pesan sukses
                                                Toast.makeText(SignActivity.this, "Pendaftaran Berhasil!", Toast.LENGTH_SHORT).show();
                                                
                                                // Arahkan ke halaman Login dan tutup activity ini
                                                Intent intent = new Intent(SignActivity.this, LoginActivity.class); // Ganti LoginActivity.class jika nama file Anda berbeda
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // Jika gagal menyimpan ke firestore
                                                Toast.makeText(SignActivity.this, "Gagal menyimpan data profil.", Toast.LENGTH_SHORT).show();
                                                buttonSignUp.setEnabled(true); // Aktifkan tombol kembali
                                            }
                                        }
                                    });

                        } else {
                            // Jika gagal membuat akun di Authentication
                            // progressBar.setVisibility(View.GONE);
                            buttonSignUp.setEnabled(true);
                            Toast.makeText(SignActivity.this, "Pendaftaran Gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}