package activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deliveryfood.R;

import java.util.ArrayList;
import java.util.List;

import adapters.AvatarAdapter;

public class SelectAvatarActivity extends AppCompatActivity implements AvatarAdapter.OnAvatarListener {

    private RecyclerView avatarRecyclerView;
    private AvatarAdapter avatarAdapter;
    private List<String> avatarList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_avatar);

        // Setup Toolbar dan Tombol Kembali
        Toolbar toolbar = findViewById(R.id.toolbarAvatar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageButton backButton = findViewById(R.id.backButtonAvatar);
        backButton.setOnClickListener(v -> finish()); // Menutup activity ini

        // Inisialisasi RecyclerView
        avatarRecyclerView = findViewById(R.id.avatarRecyclerView);
        avatarList = new ArrayList<>();

        // Panggil fungsi untuk mengisi daftar avatar
        loadAvatarList();

        // Setup Adapter
        avatarAdapter = new AvatarAdapter(this, avatarList, this);
        avatarRecyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 kolom grid
        avatarRecyclerView.setAdapter(avatarAdapter);
    }

    private void loadAvatarList() {
        // TAMBAHKAN NAMA FILE AVATAR ANDA DI SINI
        // Nama harus sama persis dengan nama file di res/drawable (tanpa .png)
        avatarList.add("avatar_1");
        avatarList.add("avatar_2");
        avatarList.add("avatar_3");
        avatarList.add("avatar_4");
        // Tambahkan sebanyak yang Anda punya...
    }

    // Implementasi dari interface OnAvatarListener
    @Override
    public void onAvatarClick(String avatarName) {
        // Buat Intent untuk mengirim data kembali ke EditProfileActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selectedAvatarName", avatarName);
        setResult(RESULT_OK, resultIntent);

        // Tutup activity ini dan kembali ke halaman edit profil
        finish();
    }
}