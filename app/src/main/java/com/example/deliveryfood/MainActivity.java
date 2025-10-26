package com.example.deliveryfood;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Deklarasi semua variabel
    private RecyclerView recyclerViewPopularMenu, recyclerViewPilihan;
    private MenuAdapter menuAdapter;
    private PilihanHariIniAdapter pilihanAdapter;
    private List<Menu> menuList, fullMenuList;
    private List<PilihanHariIni> pilihanList;
    private FirebaseFirestore db;
    private SearchView searchView;
    private LinearLayout pilihanHariIniLayout;
    private AppBarLayout appBarLayout;
    private BottomNavigationView bottomNavigationView;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi Database
        db = FirebaseFirestore.getInstance();

        // Inisialisasi UI
        pilihanHariIniLayout = findViewById(R.id.pilihanHariIniLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        searchView = toolbar.findViewById(R.id.search_view);
        searchView.clearFocus();
        appBarLayout = findViewById(R.id.app_bar_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();
        // Setup RecyclerView untuk Pilihan Hari Ini
        recyclerViewPilihan = findViewById(R.id.recyclerViewPilihanHariIni);
        recyclerViewPilihan.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        pilihanList = new ArrayList<>();
        pilihanAdapter = new PilihanHariIniAdapter(this, pilihanList);
        recyclerViewPilihan.setAdapter(pilihanAdapter);

        // Setup RecyclerView untuk Menu Populer
        recyclerViewPopularMenu = findViewById(R.id.recyclerViewPopularMenu);
        recyclerViewPopularMenu.setLayoutManager(new LinearLayoutManager(this));
        menuList = new ArrayList<>();
        fullMenuList = new ArrayList<>();
        menuAdapter = new MenuAdapter(this, menuList);
        recyclerViewPopularMenu.setAdapter(menuAdapter);

        // Panggil semua metode penting
        loadPilihanHariIni();
        loadPopularMenu();
        setupSearchListener(); // Metode ini sekarang akan ditemukan
        setupSearchViewFocusListener();
        setupBottomNavigation();
    }

    // --- METODE YANG HILANG SEBELUMNYA ---
    private void setupSearchListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPopularMenuList(query);
                return false; // Biarkan sistem menangani keyboard
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // HAPUS logika setVisibility dari sini.
                // Fungsi ini sekarang HANYA untuk memfilter.
                filterPopularMenuList(newText);
                return true;
            }
        });

    }


    // --- FUNGSI UNTUK MENDETEKSI FOKUS PADA SEARCHVIEW ---
    private void setupSearchViewFocusListener() {
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Saat search bar diklik/aktif, gulung AppBar ke atas (collapse)
                    appBarLayout.setExpanded(false, true);
                }
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // Hapus fokus dari SearchView
                searchView.clearFocus();
                // Perintahkan AppBarLayout untuk mengembang (expand) kembali
                appBarLayout.setExpanded(true, true);
                return true; // Beri tahu sistem bahwa kita sudah menangani event ini
            }
        });

        // Listener untuk saat tombol 'X' di search bar diklik
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // Hapus fokus dari SearchView, yang akan memicu onFocusChange
                // untuk menampilkan kembali "Pilihan Hari Ini".
                searchView.clearFocus();
                return true;
            }
        });
    }

    // --- FUNGSI LAINNYA ---
    @Override
    protected void onResume() {
        super.onResume();
        // Setiap kali MainActivity kembali aktif, set item Home sebagai terpilih
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }
    private void filterPopularMenuList(String query) {
        List<Menu> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(fullMenuList);
        } else {
            for (Menu menu : fullMenuList) {
                if (menu.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(menu);
                }
            }
        }
        menuAdapter.filterList(filteredList);
    }

    private void loadPilihanHariIni() {
        db.collection("products")
                .whereEqualTo("isPilihanHariIni", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pilihanList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            PilihanHariIni pilihan = document.toObject(PilihanHariIni.class);
                            pilihan.setProductId(document.getId()); // <-- TAMBAHKAN BARIS INI
                            pilihanList.add(pilihan);
                        }
                        pilihanAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting Pilihan Hari Ini.", task.getException());
                    }
                });
    }

    private void loadPopularMenu() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        menuList.clear();
                        fullMenuList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Menu menu = document.toObject(Menu.class);
                            menu.setProductId(document.getId());
                            menuList.add(menu);
                            fullMenuList.add(menu);
                        }
                        menuAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting Popular Menu.", task.getException());
                    }
                });
    }
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Set Home sebagai default
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Sudah di halaman Home
                return true;
            } else if (itemId == R.id.nav_cart) {
                // Buka CartActivity
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
                overridePendingTransition(0, 0); // Efek transisi tanpa animasi
                return true;
            } else if (itemId == R.id.nav_pesanan) {
                Toast.makeText(this, "Halaman Pesanan belum ada", Toast.LENGTH_SHORT).show();
                return false;
            } else if (itemId == R.id.nav_profile) {
                Toast.makeText(this, "Halaman Profil belum ada", Toast.LENGTH_SHORT).show();
                return false;
            }
            return false;
        });
    }
}