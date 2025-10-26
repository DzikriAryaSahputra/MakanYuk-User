package com.example.deliveryfood;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Implementasikan listener dari adapter
public class CartActivity extends AppCompatActivity implements CartAdapter.CartUpdateListener {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;
    private TextView totalPriceTextView;
    private Button checkoutButton;
    private ImageButton backButtonCart;
    private BottomNavigationView bottomNavigationView;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private static final String TAG = "CartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Cek apakah pengguna sudah login
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            // Arahkan ke LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return; // Hentikan eksekusi jika belum login
        }
        currentUserId = currentUser.getUid();

        // Inisialisasi UI
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        checkoutButton = findViewById(R.id.checkoutButton);
        backButtonCart = findViewById(R.id.backButtonCart);
        bottomNavigationView = findViewById(R.id.bottom_navigation_cart); // Hubungkan bottom nav

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarCart);
        setSupportActionBar(toolbar); // Set toolbar sebagai action bar (opsional)
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Sembunyikan judul default

        // Setup RecyclerView
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartItemList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartItemList, this); // 'this' sebagai listener
        cartRecyclerView.setAdapter(cartAdapter);

        // Atur Listener Tombol
        backButtonCart.setOnClickListener(v -> finish()); // Tutup activity saat tombol back diklik
        checkoutButton.setOnClickListener(v -> {
            // Logika untuk Sprint 5
            Toast.makeText(CartActivity.this, "Lanjut ke halaman pembayaran...", Toast.LENGTH_SHORT).show();
        });

        // Atur Listener Bottom Navigation
        setupBottomNavigation();

        // Muat data keranjang dari Firestore
        loadCartItems();
    }

    private void loadCartItems() {
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId).collection("cart")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cartItemList.clear();
                        double totalPrice = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CartItem item = document.toObject(CartItem.class);
                            cartItemList.add(item);
                            totalPrice += item.getPrice() * item.getQuantity();
                        }
                        cartAdapter.notifyDataSetChanged();
                        updateTotalPrice(totalPrice);
                    } else {
                        Log.w(TAG, "Error getting cart documents.", task.getException());
                        Toast.makeText(CartActivity.this, "Gagal memuat keranjang.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateTotalPrice(double price) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        totalPriceTextView.setText(formatRupiah.format(price));
    }

    // Metode dari interface CartUpdateListener
    @Override
    public void onQuantityChanged(String productId, int newQuantity) {
        if (currentUserId == null) return;

        if (newQuantity > 0) {
            // Update kuantitas di Firestore
            db.collection("users").document(currentUserId).collection("cart").document(productId)
                    .update("quantity", newQuantity)
                    .addOnSuccessListener(aVoid -> recalculateTotal())
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating quantity", e));
        } else {
            // Hapus item dari Firestore jika kuantitas 0
            db.collection("users").document(currentUserId).collection("cart").document(productId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Hapus item dari list lokal & hitung ulang total
                        removeItemFromList(productId);
                        recalculateTotal();
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error deleting item", e));
        }
    }

    // Fungsi bantu untuk menghapus item dari list lokal
    private void removeItemFromList(String productId) {
        for (int i = 0; i < cartItemList.size(); i++) {
            if (cartItemList.get(i).getProductId().equals(productId)) {
                cartItemList.remove(i);
                cartAdapter.notifyItemRemoved(i);
                cartAdapter.notifyItemRangeChanged(i, cartItemList.size());
                break;
            }
        }
    }

    // Fungsi bantu untuk menghitung ulang total harga
    private void recalculateTotal() {
        double totalPrice = 0;
        for (CartItem item : cartItemList) {
            totalPrice += item.getPrice() * item.getQuantity();
        }
        updateTotalPrice(totalPrice);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_cart); // Set item Keranjang sebagai aktif
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0); // Efek transisi
                finish(); // Tutup CartActivity
                return true;
            } else if (itemId == R.id.nav_cart) {
                // Sudah di halaman Keranjang, tidak perlu aksi
                return true;
            } else if (itemId == R.id.nav_pesanan) {
                // Ganti OrdersActivity.class dengan nama Activity Pesanan Anda
                // startActivity(new Intent(getApplicationContext(), OrdersActivity.class));
                // overridePendingTransition(0, 0);
                // finish();
                Toast.makeText(this, "Halaman Pesanan belum ada", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Ganti ProfileActivity.class dengan nama Activity Profil Anda
                // startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                // overridePendingTransition(0, 0);
                // finish();
                Toast.makeText(this, "Halaman Profil belum ada", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}