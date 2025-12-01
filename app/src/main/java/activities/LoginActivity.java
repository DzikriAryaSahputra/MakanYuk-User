package activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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

import com.example.deliveryfood.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore; // Tambahkan Import ini


public class LoginActivity extends AppCompatActivity {

    // --- VARIABEL ---
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Tambahkan Variabel Database

    private TextView textPindahKeDaftar;
    private TextView textPindahKeForget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // --- INISIALISASI FIREBASE ---
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Inisialisasi Firestore

        // --- INISIALISASI VIEW ---
        editTextEmail = findViewById(R.id.editTextTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextTextPassword);
        buttonLogin = findViewById(R.id.button);
        textPindahKeDaftar = findViewById(R.id.textView3);
        textPindahKeForget = findViewById(R.id.textView2);

        // Boilerplate Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- LISTENER ---
        buttonLogin.setOnClickListener(v -> loginUser());

        textPindahKeDaftar.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignActivity.class);
            startActivity(intent);
        });

        textPindahKeForget.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgetActivity.class);
            startActivity(intent);
        });
    }

    // --- LOGIKA LOGIN UTAMA ---
    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // 1. Validasi Input
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

        // 2. Proses Login ke Firebase Auth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login Auth Berhasil, TAPI jangan masuk dulu!
                            // Cek status akun di Firestore (apakah dihapus admin?)
                            checkUserStatus(mAuth.getCurrentUser().getUid());
                        } else {
                            // Login gagal (Password salah / Email tidak ada di Auth)
                            Toast.makeText(LoginActivity.this, "Login Gagal: Periksa Email atau Password.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // --- METHOD BARU: CEK STATUS DI DATABASE ---
    private void checkUserStatus(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // BERHASIL: Data user ada di database (Akun Aktif)
                        Toast.makeText(LoginActivity.this, "Login Berhasil.", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // GAGAL: Data user TIDAK ADA (Sudah dihapus Admin)
                        // Paksa Logout dari Auth
                        mAuth.signOut();

                        Toast.makeText(LoginActivity.this, "Akun Anda telah dihapus atau dinonaktifkan oleh Admin.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Error koneksi saat cek database
                    mAuth.signOut(); // Logout demi keamanan
                    Toast.makeText(LoginActivity.this, "Gagal memverifikasi akun: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}