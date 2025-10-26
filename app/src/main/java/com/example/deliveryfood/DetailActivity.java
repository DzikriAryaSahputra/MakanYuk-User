package com.example.deliveryfood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    // Deklarasi komponen UI
    private TextView variantSubtitle, variantTitle, descriptionTitle;
    private ImageView detailImageView;
    private TextView detailMenuName, detailMenuDescription, detailPrice, detailRatingText, textViewQuantity;
    private ImageButton backButton, buttonMinus, buttonPlus, addToCartButton;
    private Button buyNowButton;
    private RadioGroup radioGroupVariant;

    private FirebaseFirestore db;
    private Menu currentMenu;
    private String productId;
    private static final String TAG = "DetailActivity";
    private int quantity = 1; // Nilai awal kuantitas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = FirebaseFirestore.getInstance();

        // Hubungkan semua komponen UI dengan ID di XML
        detailImageView = findViewById(R.id.detailImageView);
        detailMenuName = findViewById(R.id.detailMenuName);
        detailMenuDescription = findViewById(R.id.detailMenuDescription);
        detailPrice = findViewById(R.id.detailPrice);
        detailRatingText = findViewById(R.id.detailRatingText);
        textViewQuantity = findViewById(R.id.textViewQuantity);
        descriptionTitle = findViewById(R.id.descriptionTitle);

        backButton = findViewById(R.id.backButton);
        buttonMinus = findViewById(R.id.buttonMinus);
        buttonPlus = findViewById(R.id.buttonPlus);
        addToCartButton = findViewById(R.id.addToCartButton);
        buyNowButton = findViewById(R.id.buyNowButton);

        radioGroupVariant = findViewById(R.id.radioGroupVariant);
        variantSubtitle = findViewById(R.id.variantSubtitle);
        variantTitle = findViewById(R.id.variantTitle);
        radioGroupVariant = findViewById(R.id.radioGroupVariant);

        // Ambil ID produk yang dikirim dari MainActivity
        productId = getIntent().getStringExtra("PRODUCT_ID");

        if (productId != null && !productId.isEmpty()) {
            loadProductDetails(productId);
        } else {
            Toast.makeText(this, "Error: Tidak ada data produk.", Toast.LENGTH_SHORT).show();
            finish(); // Tutup activity jika tidak ada ID
        }

        // Atur fungsi untuk tombol-tombol
        setupButtonListeners();
    }

    private void loadProductDetails(String id) {
        db.collection("products").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentMenu = documentSnapshot.toObject(Menu.class);
                        if (currentMenu != null) {
                            currentMenu.setProductId(id);
                            detailMenuName.setText(currentMenu.getName());
                            detailMenuDescription.setText(currentMenu.getDescription());
                            detailRatingText.setText(String.valueOf(currentMenu.getRating()));
                            displayVariants(currentMenu.getVariants());
                            descriptionTitle.setText(currentMenu.getName());

                            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
                            detailPrice.setText(formatRupiah.format(currentMenu.getPrice()));


                            Glide.with(this)
                                    .load(currentMenu.getImageUrl())
                                    .into(detailImageView);
                        }
                    } else {
                        Toast.makeText(this, "Produk tidak ditemukan.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat data.", Toast.LENGTH_SHORT).show();
                    finish();

                });

    }
    // --- FUNGSI BARU UNTUK MENAMPILKAN VARIAN ---
    private void displayVariants(List<String> variants) {
        // Pertama, bersihkan dulu RadioGroup dari sisa view sebelumnya
        radioGroupVariant.removeAllViews();

        if (variants != null && !variants.isEmpty()) {
            // Jika ada varian, tampilkan judul dan RadioGroup
            variantSubtitle.setVisibility(View.VISIBLE);
            variantTitle.setVisibility(View.VISIBLE);
            radioGroupVariant.setVisibility(View.VISIBLE);

            // Loop untuk setiap nama varian di dalam list
            for (String variantName : variants) {
                // Buat RadioButton baru secara programatik
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(variantName);
                radioButton.setTextSize(16);

                // Tambahkan RadioButton ke dalam RadioGroup
                radioGroupVariant.addView(radioButton);
            }
            // Set varian pertama sebagai pilihan default
            if (radioGroupVariant.getChildCount() > 0) {
                ((RadioButton) radioGroupVariant.getChildAt(0)).setChecked(true);
            }

        } else {
            // Jika tidak ada varian, sembunyikan seluruh bagian varian
            variantSubtitle.setVisibility(View.GONE);
            variantTitle.setVisibility(View.GONE);
            radioGroupVariant.setVisibility(View.GONE);
        }
    }
    private void setupButtonListeners() {
        // Tombol kembali
        backButton.setOnClickListener(v -> {
            onBackPressed(); // Fungsi bawaan untuk kembali
        });

        // Tombol tambah kuantitas
        buttonPlus.setOnClickListener(v -> {
            quantity++;
            textViewQuantity.setText(String.valueOf(quantity));
        });

        // Tombol kurang kuantitas
        buttonMinus.setOnClickListener(v -> {
            if (quantity > 1) { // Kuantitas tidak bisa kurang dari 1
                quantity--;
                textViewQuantity.setText(String.valueOf(quantity));
            }
        });

        // Tombol tambah ke keranjang
        addToCartButton.setOnClickListener(v -> {
            addItemToCart(); // Panggil fungsi baru
        });

        // Tombol beli sekarang
        buyNowButton.setOnClickListener(v -> {
            // Logika untuk Sprint 5
            Toast.makeText(DetailActivity.this, "Lanjut ke halaman checkout...", Toast.LENGTH_SHORT).show();
        });
    }
    private void addItemToCart() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            // Arahkan ke LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        if (currentMenu == null || productId == null) {
            Toast.makeText(this, "Gagal menambahkan ke keranjang: Data produk tidak lengkap.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // Buat objek data untuk disimpan ke keranjang
        Map<String, Object> cartItemData = new HashMap<>();
        cartItemData.put("productId", productId); // Simpan ID produk asli
        cartItemData.put("name", currentMenu.getName());
        cartItemData.put("price", currentMenu.getPrice());
        cartItemData.put("imageUrl", currentMenu.getImageUrl());
        cartItemData.put("quantity", quantity); // Ambil dari tombol +/-

        // Simpan ke sub-koleksi 'cart' di dokumen pengguna
        // Gunakan ID produk sebagai ID dokumen untuk mencegah duplikasi
        db.collection("users").document(userId).collection("cart").document(productId)
                .set(cartItemData) // Gunakan .set() untuk menimpa jika sudah ada atau membuat baru
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DetailActivity.this, currentMenu.getName() + " ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetailActivity.this, "Gagal menambahkan ke keranjang: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error adding item to cart", e);
                });
    }
}