package activities; // Pastikan package ini benar sesuai lokasi file

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.deliveryfood.R;
import models.CartItem; // Import Model dari package baru
import models.Order;    // Import Model dari package baru

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetailOrderActivity extends AppCompatActivity {

    // Deklarasi Variabel UI
    private TextView tvOrderId, tvOrderDate, tvStatus, tvAddress, tvItems;
    private TextView tvPaymentMethod, tvDeliveryFee, tvTotal;

    // Variabel untuk Penolakan
    private View cardRejection;
    private TextView tvRejectionReason;

    private ImageView btnBack;
    private Button btnRate; // Tombol Rating

    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order);

        // 1. Inisialisasi View (Hubungkan dengan XML)
        tvOrderId = findViewById(R.id.detailOrderId);
        tvOrderDate = findViewById(R.id.detailOrderDate);
        tvStatus = findViewById(R.id.detailOrderStatus);
        tvAddress = findViewById(R.id.detailAddress);
        tvItems = findViewById(R.id.detailItems);
        tvPaymentMethod = findViewById(R.id.detailPaymentMethod);
        tvDeliveryFee = findViewById(R.id.detailDeliveryFee);
        tvTotal = findViewById(R.id.detailTotal);

        // Inisialisasi View Penolakan
        cardRejection = findViewById(R.id.cardRejectionReason);
        tvRejectionReason = findViewById(R.id.tvRejectionReason);

        btnBack = findViewById(R.id.btnBackDetail);
        btnRate = findViewById(R.id.btnRateOrder);

        // 2. Listener Tombol Kembali
        btnBack.setOnClickListener(v -> finish());

        // 3. Ambil Data dari Intent
        if (getIntent() != null && getIntent().hasExtra("ORDER_DATA")) {
            currentOrder = (Order) getIntent().getSerializableExtra("ORDER_DATA");
            if (currentOrder != null) {
                displayData(currentOrder);
                checkRatingStatus(); // Cek apakah tombol harus muncul
            }
        } else {
            Toast.makeText(this, "Data pesanan tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 4. Listener Klik Tombol Rating
        btnRate.setOnClickListener(v -> showRatingDialog());
    }

    // --- FUNGSI MENAMPILKAN DATA KE LAYAR ---
    private void displayData(Order order) {
        // Tampilkan ID
        if (order.getOrderId() != null) {
            tvOrderId.setText("Order #" + order.getOrderId().toUpperCase());
        }

        // Tampilkan Status
        tvStatus.setText(order.getStatus());

        // Tampilkan Tanggal
        if (order.getOrderTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            tvOrderDate.setText(sdf.format(order.getOrderTimestamp()));
        }

        // Tampilkan Alamat (Menggunakan Helper Method dari Model Order)
        tvAddress.setText(order.getShippingAddressString());

        // Tampilkan Daftar Item (Looping)
        StringBuilder itemsBuilder = new StringBuilder();
        if (order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                itemsBuilder.append(item.getQuantity())
                        .append("x ")
                        .append(item.getFoodName())
                        .append(" (")
                        .append(formatRupiah(item.getPrice()))
                        .append(")\n");
            }
        }
        tvItems.setText(itemsBuilder.toString());

        // Tampilkan Rincian Biaya
        tvPaymentMethod.setText(order.getPaymentMethod() != null ? order.getPaymentMethod().toUpperCase() : "-");
        tvDeliveryFee.setText(formatRupiah(order.getDeliveryFee()));
        tvTotal.setText(formatRupiah(order.getTotal()));

        // --- LOGIKA TAMPILKAN ALASAN TOLAK (JIKA ADA) ---
        if ("Dibatalkan".equals(order.getStatus()) &&
                order.getRejectionReason() != null &&
                !order.getRejectionReason().isEmpty()) {

            cardRejection.setVisibility(View.VISIBLE);
            tvRejectionReason.setText(order.getRejectionReason());
        } else {
            cardRejection.setVisibility(View.GONE);
        }
    }

    // --- LOGIKA TOMBOL RATING (Hanya Muncul jika Selesai) ---
    private void checkRatingStatus() {
        // Pastikan Status adalah "Selesai" (Huruf Besar Sesuai Admin)
        if ("Selesai".equalsIgnoreCase(currentOrder.getStatus())) {

            // Jika rating masih 0, berarti belum dinilai
            if (currentOrder.getRating() == 0) {
                btnRate.setVisibility(View.VISIBLE);
                btnRate.setText("Beri Penilaian");
                btnRate.setEnabled(true);
            } else {
                // Jika sudah ada rating, tampilkan info tapi disable tombol
                btnRate.setVisibility(View.VISIBLE);
                btnRate.setText("Terima kasih atas penilaian Anda ★");
                btnRate.setEnabled(false);
                btnRate.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            // Jika status bukan Selesai, sembunyikan tombol
            btnRate.setVisibility(View.GONE);
        }
    }

    // --- TAMPILKAN DIALOG POPUP UNTUK RATING ---
    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_rating, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if(dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText etReview = view.findViewById(R.id.etReview);
        Button btnSubmit = view.findViewById(R.id.btnSubmitRating);

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String review = etReview.getText().toString();

            if (rating == 0) {
                Toast.makeText(this, "Mohon pilih bintang terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }
            submitRatingToFirestore(rating, review, dialog);
        });

        dialog.show();
    }

    // --- SIMPAN RATING KE FIREBASE (ORDER & PRODUCT) ---
    private void submitRatingToFirestore(float rating, String review, AlertDialog dialog) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Update Dokumen Order (Menyimpan histori rating user)
        db.collection("orders").document(currentOrder.getOrderId())
                .update("rating", rating, "review", review)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Ulasan terkirim!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                    // Update UI Lokal langsung (tanpa reload)
                    currentOrder.setRating(rating);
                    currentOrder.setReview(review);
                    checkRatingStatus();

                    // 2. Update Rata-rata Rating di Produk Asli
                    updateProductRatings(rating);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal mengirim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- MENGHITUNG RATA-RATA RATING BARU UNTUK MENU ---
    private void updateProductRatings(float newRating) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (currentOrder.getItems() != null) {
            // Loop semua item dalam pesanan ini
            for (CartItem item : currentOrder.getItems()) {
                String productId = item.getProductId();

                if (productId != null) {
                    DocumentReference productRef = db.collection("products").document(productId);

                    // Gunakan Transaction agar perhitungan aman
                    db.runTransaction(transaction -> {
                        DocumentSnapshot snapshot = transaction.get(productRef);

                        double oldRating = snapshot.contains("rating") ? snapshot.getDouble("rating") : 0.0;
                        long oldCount = snapshot.contains("ratingCount") ? snapshot.getLong("ratingCount") : 0;

                        // Rumus Rata-rata Baru
                        double newAverage = ((oldRating * oldCount) + newRating) / (oldCount + 1);

                        transaction.update(productRef, "rating", newAverage);
                        transaction.update(productRef, "ratingCount", oldCount + 1);
                        return null;
                    });
                }
            }
        }
    }

    // Helper untuk format mata uang
    private String formatRupiah(double number) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return formatRupiah.format(number);
    }
}