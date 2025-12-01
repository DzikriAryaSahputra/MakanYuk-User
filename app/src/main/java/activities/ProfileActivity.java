package activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout; // Pastikan ini di-import
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.deliveryfood.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private TextView profileNameTextView, profileEmailTextView;
    private Button editProfileButton, logoutButton; // Gunakan Button atau AppCompatButton sesuai layout
    private ImageButton backButtonProfile;
    private RelativeLayout alamatLayout, pusatBantuanLayout; // Untuk opsi menu
    private BottomNavigationView bottomNavigationView;
    private View loadingOverlay;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        loadingOverlay = findViewById(R.id.loadingOverlay);

        // 2. TAMPILKAN LOADING SEKARANG
        loadingOverlay.setVisibility(View.VISIBLE);
        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Cek jika pengguna belum login
        if (currentUser == null) {
            goToLoginActivity(); // Panggil fungsi bantu
            return;
        }

        // Inisialisasi Komponen UI
        profileImageView = findViewById(R.id.profileImageView);
        profileNameTextView = findViewById(R.id.profileNameTextView);
        profileEmailTextView = findViewById(R.id.profileEmailTextView);
        editProfileButton = findViewById(R.id.editProfileButton);
        logoutButton = findViewById(R.id.logoutButton);
        backButtonProfile = findViewById(R.id.backButtonProfile);
        alamatLayout = findViewById(R.id.alamatLayout);
        pusatBantuanLayout = findViewById(R.id.pusatBantuanLayout);
        bottomNavigationView = findViewById(R.id.bottom_navigation_profile);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Atur Listener Tombol
        setupButtonClickListeners();

        // Atur Bottom Navigation
        setupBottomNavigation();

        // HAPUS loadUserProfile() dari sini
    }

    // --- PERBAIKAN BUG REFRESH: Pindahkan pemuatan data ke onResume ---
    @Override
    protected void onResume() {
        super.onResume();
        // Set item bottom nav sebagai aktif
        bottomNavigationView.setSelectedItemId(R.id.nav_profile); // Gunakan ID yang benar
        // Muat (atau muat ulang) data profil setiap kali halaman ini aktif
        loadUserProfile();
    }
    // ---------------------------------------------------------------

    private void loadUserProfile() {
        String userId = currentUser.getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Ambil data dari Firestore
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String avatarName = documentSnapshot.getString("profileImageUrl"); // Ini nama avatar, misal "avatar_1"

                        // Tampilkan data ke UI
                        profileNameTextView.setText(name != null ? name : "Nama Belum Diatur");
                        profileEmailTextView.setText(email);

                        // --- PERBAIKAN BUG FOTO TIDAK MUNCUL ---
                        // Hapus blok kode Glide yang dikomentari dan ganti dengan ini:
                        if (avatarName != null && !avatarName.isEmpty()) {
                            // Dapatkan ID resource dari nama file di folder drawable
                            int imageResId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
                            if (imageResId != 0) {
                                // Jika ditemukan, tampilkan gambar
                                profileImageView.setImageResource(imageResId);
                            } else {
                                // Jika tidak ditemukan, tampilkan gambar default
                                profileImageView.setImageResource(R.mipmap.ic_launcher_round);
                            }
                        } else {
                            // Jika field profileImageUrl kosong, tampilkan gambar default
                            profileImageView.setImageResource(R.mipmap.ic_launcher_round);
                        }
                        // ------------------------------------

                    } else {
                        Log.d(TAG, "Dokumen profil tidak ditemukan.");
                        profileEmailTextView.setText(currentUser.getEmail()); // Fallback
                    }
                    loadingOverlay.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    loadingOverlay.setVisibility(View.GONE);
                    Log.w(TAG, "Gagal memuat profil.", e);
                    Toast.makeText(ProfileActivity.this, "Gagal memuat profil.", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupButtonClickListeners() {
        backButtonProfile.setOnClickListener(v -> finish()); // Kembali

        editProfileButton.setOnClickListener(v -> {
            // Arahkan ke EditProfileActivity
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
            // HAPUS Toast ini karena fiturnya sudah ada
            // Toast.makeText(this, "Fitur Edit Profil belum ada", Toast.LENGTH_SHORT).show();
        });

        // Placeholder untuk opsi lain
        alamatLayout.setOnClickListener(v -> {
                    Intent intent = new Intent(ProfileActivity.this, AddressActivity.class);
                    startActivity(intent);
                });
        pusatBantuanLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HelpCenterActivity.class);
            startActivity(intent);
        });
        // Tombol Logout
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(ProfileActivity.this, "Berhasil Keluar", Toast.LENGTH_SHORT).show();
            goToLoginActivity(); // Panggil fungsi bantu
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Pastikan ID ini sesuai dengan file bottom_nav_menu.xml Anda
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_pesanan) {
                // --- BAGIAN INI DIPERBARUI ---
                // Buka MyOrdersActivity (Pesanan Saya)
                startActivity(new Intent(getApplicationContext(), MyOrdersActivity.class));
                overridePendingTransition(0, 0);
                return false;
            }
            else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    // Fungsi bantu untuk pindah ke Login
    private void goToLoginActivity() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}