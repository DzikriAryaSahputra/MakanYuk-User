package activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deliveryfood.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap; // Tambahan Import
import java.util.List;
import java.util.Locale;
import java.util.Map; // Tambahan Import

import adapters.CheckoutAdapter;
import models.Address;
import models.CartItem;
import models.Order;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView checkoutRecyclerView;
    private CheckoutAdapter checkoutAdapter;
    private List<CartItem> cartItemList;

    private TextView checkoutAddressLabel, checkoutAddressDetails;
    private TextView checkoutSubtotal, checkoutDeliveryFee, checkoutTotal;
    private MaterialCardView paymentCardCOD, paymentCardVA;
    private Button buttonCreateOrder, checkoutChangeAddressButton;
    private ImageButton backButtonCheckout;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Address defaultAddress;

    private String selectedPaymentMethod = "COD";
    private double subtotal = 0;
    private double deliveryFee = 10000;

    private static final String TAG = "CheckoutActivity";

    // Flag untuk mode Beli Sekarang
    private boolean isBuyNowMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            goToLoginActivity();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        selectPaymentMethod("COD");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDefaultAddress();

        // Cek alur Beli Sekarang atau Keranjang
        if (getIntent().hasExtra("BUY_NOW_ITEM")) {
            isBuyNowMode = true;
            loadBuyNowItem();
        } else {
            isBuyNowMode = false;
            loadCartItems();
        }
    }

    private void loadBuyNowItem() {
        CartItem item = (CartItem) getIntent().getSerializableExtra("BUY_NOW_ITEM");
        if (item != null) {
            cartItemList.clear();
            cartItemList.add(item);
            checkoutAdapter.notifyDataSetChanged();

            subtotal = item.getPrice() * item.getQuantity();
            calculateTotals();
        } else {
            Toast.makeText(this, "Gagal memuat item.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- PERBAIKAN UTAMA DI SINI (COD) ---
    private void createOrderCOD() {
        if (currentUser == null || defaultAddress == null || cartItemList.isEmpty()) {
            Toast.makeText(this, "Data tidak lengkap.", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonCreateOrder.setEnabled(false);
        buttonCreateOrder.setText("Memproses...");

        String userId = currentUser.getUid();
        double total = subtotal + deliveryFee;

        // 1. UBAH ADDRESS JADI MAP AGAR COCOK DENGAN ORDER.JAVA
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("fullAddress", defaultAddress.getFullAddress());
        addressMap.put("recipientName", defaultAddress.getRecipientName());
        addressMap.put("phoneNumber", defaultAddress.getPhoneNumber());
        addressMap.put("label", defaultAddress.getLabel());
        addressMap.put("notes", defaultAddress.getNotes());
        addressMap.put("zipCode", defaultAddress.getZipCode());
        addressMap.put("isDefault", defaultAddress.getIsDefault());

        // 2. BUAT ORDER MENGGUNAKAN MAP
        Order order = new Order(
                userId,
                addressMap, // Gunakan Map ini, bukan objek defaultAddress
                cartItemList,
                subtotal,
                deliveryFee,
                total,
                "COD",
                "Menunggu Konfirmasi"
        );

        DocumentReference newOrderRef = db.collection("orders").document();
        order.setOrderId(newOrderRef.getId());

        newOrderRef.set(order)
                .addOnSuccessListener(aVoid -> {
                    if (isBuyNowMode) {
                        goToSuccessPage();
                    } else {
                        clearCart(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    buttonCreateOrder.setEnabled(true);
                    buttonCreateOrder.setText("Buat Pesanan");
                    Toast.makeText(CheckoutActivity.this, "Gagal membuat pesanan.", Toast.LENGTH_SHORT).show();
                });
    }

    // --- PERBAIKAN UTAMA DI SINI (VA) ---
    private void createOrderVA() {
        if (currentUser == null || defaultAddress == null || cartItemList.isEmpty()) {
            Toast.makeText(this, "Data tidak lengkap", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonCreateOrder.setEnabled(false);
        buttonCreateOrder.setText("Memproses...");

        String userId = currentUser.getUid();
        double total = subtotal + deliveryFee;

        // 1. UBAH ADDRESS JADI MAP AGAR COCOK DENGAN ORDER.JAVA
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("fullAddress", defaultAddress.getFullAddress());
        addressMap.put("recipientName", defaultAddress.getRecipientName());
        addressMap.put("phoneNumber", defaultAddress.getPhoneNumber());
        addressMap.put("label", defaultAddress.getLabel());
        addressMap.put("notes", defaultAddress.getNotes());
        addressMap.put("zipCode", defaultAddress.getZipCode());
        addressMap.put("isDefault", defaultAddress.getIsDefault());

        // 2. BUAT ORDER MENGGUNAKAN MAP
        Order order = new Order(
                userId,
                addressMap, // Gunakan Map ini
                cartItemList,
                subtotal,
                deliveryFee,
                total,
                "Virtual Account",
                "Menunggu Pembayaran"
        );

        DocumentReference newOrderRef = db.collection("orders").document();
        order.setOrderId(newOrderRef.getId());

        newOrderRef.set(order)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CheckoutActivity.this, "Pesanan VA berhasil dibuat.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(CheckoutActivity.this, PaymentInstructionsActivity.class);
                    intent.putExtra("TOTAL_AMOUNT", total);
                    if (isBuyNowMode) {
                        intent.putExtra("IS_BUY_NOW", true);
                    }
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    buttonCreateOrder.setEnabled(true);
                    buttonCreateOrder.setText("Buat Pesanan");
                    Toast.makeText(CheckoutActivity.this, "Gagal membuat pesanan.", Toast.LENGTH_SHORT).show();
                });
    }

    private void clearCart(String userId) {
        CollectionReference cartRef = db.collection("users").document(userId).collection("cart");
        cartRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                WriteBatch batch = db.batch();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    batch.delete(document.getReference());
                }
                batch.commit().addOnSuccessListener(aVoid -> {
                    goToSuccessPage();
                }).addOnFailureListener(e -> {
                    Toast.makeText(CheckoutActivity.this, "Gagal bersihkan keranjang.", Toast.LENGTH_SHORT).show();
                    goToSuccessPage();
                });
            }
        });
    }

    private void goToSuccessPage() {
        Intent intent = new Intent(CheckoutActivity.this, SuccessActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        checkoutRecyclerView = findViewById(R.id.checkoutRecyclerView);
        checkoutAddressLabel = findViewById(R.id.checkoutAddressLabel);
        checkoutAddressDetails = findViewById(R.id.checkoutAddressDetails);
        checkoutSubtotal = findViewById(R.id.checkoutSubtotal);
        checkoutDeliveryFee = findViewById(R.id.checkoutDeliveryFee);
        checkoutTotal = findViewById(R.id.checkoutTotal);
        paymentCardCOD = findViewById(R.id.paymentCardCOD);
        paymentCardVA = findViewById(R.id.paymentCardVA);
        buttonCreateOrder = findViewById(R.id.buttonCreateOrder);
        checkoutChangeAddressButton = findViewById(R.id.checkoutChangeAddressButton);
        backButtonCheckout = findViewById(R.id.backButtonCheckout);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarCheckout);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupRecyclerView() {
        cartItemList = new ArrayList<>();
        checkoutAdapter = new CheckoutAdapter(this, cartItemList);
        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutRecyclerView.setAdapter(checkoutAdapter);
        checkoutRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        backButtonCheckout.setOnClickListener(v -> finish());
        checkoutChangeAddressButton.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutActivity.this, AddressActivity.class);
            startActivity(intent);
        });
        paymentCardCOD.setOnClickListener(v -> selectPaymentMethod("COD"));
        paymentCardVA.setOnClickListener(v -> selectPaymentMethod("VA"));

        buttonCreateOrder.setOnClickListener(v -> {
            if (defaultAddress == null) {
                Toast.makeText(this, "Silakan tambahkan alamat terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedPaymentMethod.equals("COD")) {
                createOrderCOD();
            } else if (selectedPaymentMethod.equals("VA")) {
                createOrderVA();
            }
        });
    }

    private void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;
        int orangeColor = ContextCompat.getColor(this, R.color.orange);
        int greyColor = Color.parseColor("#E0E0E0");
        int orangeTint = orangeColor;
        int greyTint = Color.parseColor("#757575");

        if (method.equals("COD")) {
            paymentCardCOD.setStrokeColor(orangeColor);
            paymentCardCOD.setStrokeWidth(dpToPx(2));
            ((ImageView) paymentCardCOD.findViewById(R.id.codIcon)).setColorFilter(orangeTint);

            paymentCardVA.setStrokeColor(greyColor);
            paymentCardVA.setStrokeWidth(dpToPx(1));
            ((ImageView) paymentCardVA.findViewById(R.id.vaIcon)).setColorFilter(greyTint);
        } else if (method.equals("VA")) {
            paymentCardCOD.setStrokeColor(greyColor);
            paymentCardCOD.setStrokeWidth(dpToPx(1));
            ((ImageView) paymentCardCOD.findViewById(R.id.codIcon)).setColorFilter(greyTint);

            paymentCardVA.setStrokeColor(orangeColor);
            paymentCardVA.setStrokeWidth(dpToPx(2));
            ((ImageView) paymentCardVA.findViewById(R.id.vaIcon)).setColorFilter(orangeTint);
        }
    }

    private void loadDefaultAddress() {
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("addresses")
                .whereEqualTo("isDefault", true)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        defaultAddress = task.getResult().getDocuments().get(0).toObject(Address.class);
                        updateAddressUI();
                    } else {
                        loadFirstAddress();
                    }
                });
    }

    private void loadFirstAddress() {
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("addresses")
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        defaultAddress = task.getResult().getDocuments().get(0).toObject(Address.class);
                        updateAddressUI();
                    } else {
                        Log.d(TAG, "Tidak ada alamat tersimpan.");
                        checkoutAddressLabel.setText("Alamat Belum Diatur");
                        checkoutAddressDetails.setText("Belum ada alamat tersimpan. Silakan tambah alamat.");
                        defaultAddress = null;
                    }
                });
    }

    private void updateAddressUI() {
        if (defaultAddress != null) {
            checkoutAddressLabel.setText(defaultAddress.getLabel() + (defaultAddress.getIsDefault() ? " (Utama)" : ""));
            String details = defaultAddress.getRecipientName() + " (" + defaultAddress.getPhoneNumber() + ")\n" + defaultAddress.getFullAddress();
            checkoutAddressDetails.setText(details);
        }
    }

    private void loadCartItems() {
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("cart")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cartItemList.clear();
                        subtotal = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CartItem item = document.toObject(CartItem.class);
                            cartItemList.add(item);
                            subtotal += item.getPrice() * item.getQuantity();
                        }
                        checkoutAdapter.notifyDataSetChanged();
                        calculateTotals();
                    } else {
                        Log.w(TAG, "Error getting cart.", task.getException());
                    }
                });
    }

    private void calculateTotals() {
        double total = subtotal + deliveryFee;
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        checkoutSubtotal.setText(formatRupiah.format(subtotal));
        checkoutDeliveryFee.setText(formatRupiah.format(deliveryFee));
        checkoutTotal.setText(formatRupiah.format(total));
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}