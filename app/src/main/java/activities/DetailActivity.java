package activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioButton; // Import yang mungkin hilang
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import com.example.deliveryfood.R;
import com.google.firebase.auth.FirebaseAuth; // Import
import com.google.firebase.auth.FirebaseUser; // Import
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

import models.CartItem;
import models.Menu;

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
    private int quantity = 1;

    // --- PERBAIKAN 1: Deklarasi Firebase Auth ---
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    // -----------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = FirebaseFirestore.getInstance();

        // --- PERBAIKAN 1: Inisialisasi Firebase Auth ---
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        // ---------------------------------------------

        // Hubungkan semua komponen UI
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
        // radioGroupVariant = findViewById(R.id.radioGroupVariant); // Duplikat, hapus

        productId = getIntent().getStringExtra("PRODUCT_ID");

        if (productId != null && !productId.isEmpty()) {
            loadProductDetails(productId);
        } else {
            Toast.makeText(this, "Error: Tidak ada data produk.", Toast.LENGTH_SHORT).show();
            finish();
        }

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

    private void displayVariants(List<String> variants) {
        radioGroupVariant.removeAllViews();
        if (variants != null && !variants.isEmpty()) {
            variantSubtitle.setVisibility(View.VISIBLE);
            variantTitle.setVisibility(View.VISIBLE);
            radioGroupVariant.setVisibility(View.VISIBLE);

            for (String variantName : variants) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(variantName);
                radioButton.setTextSize(16);
                radioGroupVariant.addView(radioButton);
            }
            if (radioGroupVariant.getChildCount() > 0) {
                ((RadioButton) radioGroupVariant.getChildAt(0)).setChecked(true);
            }
        } else {
            variantSubtitle.setVisibility(View.GONE);
            variantTitle.setVisibility(View.GONE);
            radioGroupVariant.setVisibility(View.GONE);
        }
    }

    // --- PERBAIKAN 2: Fungsi baru untuk mengambil varian ---
    private String getSelectedVariant() {
        if (radioGroupVariant.getVisibility() == View.GONE) {
            return null; // Tidak ada varian
        }

        int selectedId = radioGroupVariant.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            return selectedRadioButton.getText().toString();
        }
        return null; // Tidak ada yang dipilih
    }
    // ----------------------------------------------------

    private void setupButtonListeners() {
        backButton.setOnClickListener(v -> onBackPressed());

        buttonPlus.setOnClickListener(v -> {
            quantity++;
            textViewQuantity.setText(String.valueOf(quantity));
        });

        buttonMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                textViewQuantity.setText(String.valueOf(quantity));
            }
        });

        addToCartButton.setOnClickListener(v -> {
            addItemToCart(); // Panggil fungsi yang diperbarui
        });

        buyNowButton.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            if (currentMenu == null) {
                Toast.makeText(this, "Data produk belum dimuat.", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- PERBAIKAN 2 (Lanjutan): Perbarui logika "Beli Sekarang" ---
            String selectedVariant = getSelectedVariant();

            // Validasi jika varian wajib diisi
            if (radioGroupVariant.getVisibility() == View.VISIBLE && selectedVariant == null) {
                Toast.makeText(this, "Silakan pilih varian terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            CartItem buyNowItem = new CartItem(
                    productId,
                    currentMenu.getName(),
                    currentMenu.getPrice(),
                    currentMenu.getImageUrl(),
                    quantity,
                    selectedVariant // Tambahkan varian di sini
            );

            Intent intent = new Intent(DetailActivity.this, CheckoutActivity.class);
            intent.putExtra("BUY_NOW_ITEM", buyNowItem);
            startActivity(intent);
        });
    }

    private void addItemToCart() {
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        if (currentMenu == null) {
            Toast.makeText(this, "Gagal: Data produk tidak lengkap.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // --- PERBAIKAN 2 (Lanjutan): Perbarui logika "Tambah ke Keranjang" ---
        String selectedVariant = getSelectedVariant();

        if (radioGroupVariant.getVisibility() == View.VISIBLE && selectedVariant == null) {
            Toast.makeText(this, "Silakan pilih varian terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> cartItemData = new HashMap<>();
        cartItemData.put("productId", productId);
        cartItemData.put("name", currentMenu.getName());
        cartItemData.put("price", currentMenu.getPrice());
        cartItemData.put("imageUrl", currentMenu.getImageUrl());
        cartItemData.put("quantity", quantity);
        cartItemData.put("variant", selectedVariant); // Tambahkan varian di sini

        String cartItemId = productId;
        if (selectedVariant != null) {
            cartItemId = productId + "_" + selectedVariant; // Buat ID unik
        }

        db.collection("users").document(userId).collection("cart").document(cartItemId)
                .set(cartItemData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DetailActivity.this, currentMenu.getName() + " ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetailActivity.this, "Gagal menambahkan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error adding item to cart", e);
                });
    }

    // Override onBackPressed jika Anda menggunakan animasi
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Jika Anda menambahkan animasi, panggil di sini
        // overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}