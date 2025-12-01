package activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.deliveryfood.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhone, editTextOldPassword, editTextNewPassword;
    private Button buttonSave, buttonCancel;
    private ImageButton backButtonEditProfile, editPhotoButton;
    private CircleImageView editProfileImageView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private String currentAvatarName = null;
    private String selectedAvatarName = null;
    private static final String TAG = "EditProfileActivity";

    private ActivityResultLauncher<Intent> avatarLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Tidak ada pengguna yang login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi Komponen UI (tanpa editTextEmail)
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextOldPassword = findViewById(R.id.editTextOldPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
        backButtonEditProfile = findViewById(R.id.backButtonEditProfile);
        editProfileImageView = findViewById(R.id.editProfileImageView);
        editPhotoButton = findViewById(R.id.editPhotoButton);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarEditProfile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // --- FIX 1: Nonaktifkan Tombol Simpan ---
        buttonSave.setEnabled(false);
        buttonSave.setText("Memuat data...");

        // Inisialisasi Activity Result Launcher
        avatarLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        selectedAvatarName = result.getData().getStringExtra("selectedAvatarName");
                        loadAvatarImage(selectedAvatarName);
                    }
                });

        loadCurrentUserData();
        setupButtonListeners();
    }

    private void loadCurrentUserData() {
        // Hapus referensi ke editTextEmail

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phone");
                        currentAvatarName = documentSnapshot.getString("profileImageUrl"); // Ambil nama avatar

                        editTextName.setText(name);
                        editTextPhone.setText(phone);
                        loadAvatarImage(currentAvatarName);

                        // --- FIX 1 (Lanjutan): Aktifkan Tombol Simpan ---
                        buttonSave.setEnabled(true);
                        buttonSave.setText("Simpan Perubahan");
                    } else {
                        Log.d(TAG, "Dokumen profil tidak ditemukan.");
                        buttonSave.setEnabled(true);
                        buttonSave.setText("Simpan Perubahan");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Gagal memuat data profil.", e);
                    buttonSave.setEnabled(true);
                    buttonSave.setText("Simpan Perubahan");
                });
    }

    private void loadAvatarImage(String avatarName) {
        if (avatarName != null && !avatarName.isEmpty()) {
            int imageResId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
            if (imageResId != 0) {
                editProfileImageView.setImageResource(imageResId);
            } else {
                editProfileImageView.setImageResource(R.mipmap.ic_launcher_round);
            }
        } else {
            editProfileImageView.setImageResource(R.mipmap.ic_launcher_round);
        }
    }

    private void setupButtonListeners() {
        backButtonEditProfile.setOnClickListener(v -> finish());
        buttonCancel.setOnClickListener(v -> finish());
        buttonSave.setOnClickListener(v -> saveProfileChanges());

        editPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, SelectAvatarActivity.class);
            avatarLauncher.launch(intent);
        });
    }

    private void saveProfileChanges() {
        // ... (Fungsi ini tetap sama, validasi dan panggil reauthenticate/update)
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String oldPassword = editTextOldPassword.getText().toString();
        String newPassword = editTextNewPassword.getText().toString();

        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Nama tidak boleh kosong");
            editTextName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            editTextPhone.setError("No HandPhone tidak boleh kosong");
            editTextPhone.requestFocus();
            return;
        }

        boolean isChangingPassword = !TextUtils.isEmpty(newPassword);

        if (isChangingPassword) {
            if (TextUtils.isEmpty(oldPassword)) {
                editTextOldPassword.setError("Password lama harus diisi untuk ganti password");
                editTextOldPassword.requestFocus();
                return;
            }
            if (newPassword.length() < 8) {
                editTextNewPassword.setError("Password baru minimal 8 karakter");
                editTextNewPassword.requestFocus();
                return;
            }
            reauthenticateAndChangePassword(oldPassword, newPassword, name, phone);
        } else {
            updateProfileData(name, phone, true);
        }
    }

    private void reauthenticateAndChangePassword(String oldPassword, String newPassword, String name, String phone) {
        // ... (Fungsi ini tetap sama)
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(taskPassword -> {
                                    if (taskPassword.isSuccessful()) {
                                        Log.d(TAG, "Password berhasil diperbarui.");
                                        updateProfileData(name, phone, true);
                                    } else {
                                        Log.w(TAG, "Gagal memperbarui password.", taskPassword.getException());
                                        Toast.makeText(EditProfileActivity.this, "Gagal memperbarui password.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.w(TAG, "Re-autentikasi gagal.", task.getException());
                        editTextOldPassword.setError("Password lama salah");
                        editTextOldPassword.requestFocus();
                    }
                });
    }

    private void updateProfileData(String name, String phone, boolean showToastAndFinish) {
        // ... (Fungsi ini tetap sama)
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);

        if (selectedAvatarName != null) {
            updates.put("profileImageUrl", selectedAvatarName);
        } else {
            updates.put("profileImageUrl", currentAvatarName);
        }

        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (showToastAndFinish) {
                        Log.d(TAG, "Profil berhasil diperbarui.");
                        Toast.makeText(EditProfileActivity.this, "Perubahan berhasil disimpan", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    if (showToastAndFinish) {
                        Log.w(TAG, "Gagal memperbarui profil.", e);
                        Toast.makeText(EditProfileActivity.this, "Gagal menyimpan perubahan.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}