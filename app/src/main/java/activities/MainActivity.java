package activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deliveryfood.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import adapters.MenuAdapter;
import adapters.PilihanHariIniAdapter;
import models.Menu;
import models.PilihanHariIni;

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
    private View loadingOverlay;
    private BottomNavigationView bottomNavigationView;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingOverlay = findViewById(R.id.loadingOverlay);

        // 2. TAMPILKAN LOADING SEKARANG
        loadingOverlay.setVisibility(View.VISIBLE);
        // Inisialisasi Database
        db = FirebaseFirestore.getInstance();

        // Inisialisasi UI
        pilihanHariIniLayout = findViewById(R.id.pilihanHariIniLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        searchView = toolbar.findViewById(R.id.search_view);
        searchView.clearFocus();
        appBarLayout = findViewById(R.id.app_bar_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

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
        setupSearchListener();
        setupSearchViewFocusListener();

        // Panggil Setup Navigasi Bawah (Cukup sekali saja)
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Setiap kali MainActivity kembali aktif, set item Home sebagai terpilih
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    // --- LOGIKA NAVIGASI BAWAH (UPDATED) ---
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Set Home sebagai default

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Sudah di halaman Home, tidak perlu aksi
                return true;
            }
            else if (itemId == R.id.nav_cart) {
                // Buka CartActivity
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
                overridePendingTransition(0, 0); // Tanpa animasi transisi
                return false; // Return false agar icon tidak berubah seleksi di Main
            }
            else if (itemId == R.id.nav_pesanan) {
                // --- BAGIAN INI DIPERBARUI ---
                // Buka MyOrdersActivity (Pesanan Saya)
                startActivity(new Intent(getApplicationContext(), MyOrdersActivity.class));
                overridePendingTransition(0, 0);
                return false;
            }
            else if (itemId == R.id.nav_profile) {
                // Buka ProfileActivity
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                return false;
            }

            return false;
        });
    }

    // --- LOGIKA SEARCH ---
    private void setupSearchListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPopularMenuList(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPopularMenuList(newText);
                return true;
            }
        });
    }

    private void setupSearchViewFocusListener() {
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Saat search bar aktif, gulung AppBar ke atas (collapse) agar fokus ke list
                appBarLayout.setExpanded(false, true);
            }
        });

        // Gabungan logika onClose (Hapus duplikasi)
        searchView.setOnCloseListener(() -> {
            // Hapus fokus
            searchView.clearFocus();
            // Kembalikan filter list ke semula (kosongkan query)
            filterPopularMenuList("");
            // Kembangkan AppBar lagi (Tampilkan Pilihan Hari Ini)
            appBarLayout.setExpanded(true, true);
            return false;
        });
    }

    private void filterPopularMenuList(String query) {
        List<Menu> filteredList = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(fullMenuList);
        } else {
            for (Menu menu : fullMenuList) {
                if (menu.getName() != null && menu.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(menu);
                }
            }
        }

        if (menuAdapter != null) {
            menuAdapter.filterList(filteredList);
        }
    }

    // --- LOGIKA DATABASE ---
    private void loadPilihanHariIni() {
        db.collection("products")
                .whereEqualTo("isPilihanHariIni", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pilihanList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            PilihanHariIni pilihan = document.toObject(PilihanHariIni.class);
                            pilihan.setProductId(document.getId());
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

                        loadingOverlay.setVisibility(View.GONE);
                    } else {
                        Log.w(TAG, "Error getting Popular Menu.", task.getException());
                    }
                });
    }
}