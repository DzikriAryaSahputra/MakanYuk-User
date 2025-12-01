package activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.deliveryfood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import models.Address;

public class AddAddressActivity extends AppCompatActivity {

    private EditText editAddressLabel, editAddressRecipientName, editAddressPhone, editAddressFull, editAddressZipCode, editAddressNotes;
    private Button buttonSaveAddress;
    private ImageButton backButtonAddAddress;
    private TextView toolbarTitleAddAddress;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // BARU: Variabel untuk Mode Edit
    private String currentAddressId = null;
    private Address currentAddress = null;

    private static final String TAG = "AddAddressActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi UI
        editAddressLabel = findViewById(R.id.editAddressLabel);
        editAddressRecipientName = findViewById(R.id.editAddressRecipientName);
        editAddressPhone = findViewById(R.id.editAddressPhone);
        editAddressFull = findViewById(R.id.editAddressFull);
        editAddressZipCode = findViewById(R.id.editAddressZipCode);
        editAddressNotes = findViewById(R.id.editAddressNotes);
        buttonSaveAddress = findViewById(R.id.buttonSaveAddress);
        backButtonAddAddress = findViewById(R.id.backButtonAddAddress);
        toolbarTitleAddAddress = findViewById(R.id.toolbarTitleAddAddress); // Hubungkan judul toolbar

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarAddAddress);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // --- Cek Mode Edit atau Tambah ---
        if (getIntent().hasExtra("ADDRESS_ID_TO_EDIT")) {
            currentAddressId = getIntent().getStringExtra("ADDRESS_ID_TO_EDIT");
            setupEditMode();
        } else {
            setupAddMode();
        }
        // ---------------------------------------------

        // Atur Listener Tombol
        backButtonAddAddress.setOnClickListener(v -> finish());
        buttonSaveAddress.setOnClickListener(v -> saveAddress());
    }

    private void setupAddMode() {
        toolbarTitleAddAddress.setText("Tambah Alamat Baru");
        buttonSaveAddress.setText("Simpan Alamat");
    }

    private void setupEditMode() {
        toolbarTitleAddAddress.setText("Edit Alamat");
        buttonSaveAddress.setText("Update Alamat");

        // Muat data alamat yang ada ke form
        db.collection("users").document(currentUser.getUid()).collection("addresses").document(currentAddressId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentAddress = documentSnapshot.toObject(Address.class);
                        if (currentAddress != null) {
                            editAddressLabel.setText(currentAddress.getLabel());
                            editAddressRecipientName.setText(currentAddress.getRecipientName());
                            editAddressPhone.setText(currentAddress.getPhoneNumber());
                            editAddressFull.setText(currentAddress.getFullAddress());
                            editAddressNotes.setText(currentAddress.getNotes());
                            editAddressZipCode.setText(currentAddress.getZipCode());
                        }
                    } else {
                        Toast.makeText(this, "Alamat tidak ditemukan.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat data alamat.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void saveAddress() {
        // ... (Kode validasi)
        String label = editAddressLabel.getText().toString().trim();
        String recipientName = editAddressRecipientName.getText().toString().trim();
        String phone = editAddressPhone.getText().toString().trim();
        String fullAddress = editAddressFull.getText().toString().trim();
        String zipCode = editAddressZipCode.getText().toString().trim();
        String notes = editAddressNotes.getText().toString().trim();

        if (TextUtils.isEmpty(label) || TextUtils.isEmpty(recipientName) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(fullAddress)) {
            Toast.makeText(this, "Harap isi semua field wajib", Toast.LENGTH_SHORT).show();
            return;
        }

        // objek data
        Map<String, Object> address = new HashMap<>();
        address.put("label", label);
        address.put("recipientName", recipientName);
        address.put("phoneNumber", phone);
        address.put("fullAddress", fullAddress);
        address.put("zipCode", zipCode);
        address.put("notes", notes);

        if (currentAddressId != null) {
            // --- MODE UPDATE ---
            if (currentAddress != null) {
                address.put("isDefault", currentAddress.getIsDefault());
            }

            db.collection("users").document(currentUser.getUid()).collection("addresses").document(currentAddressId)
                    .update(address)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddAddressActivity.this, "Alamat berhasil diperbarui", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(AddAddressActivity.this, "Gagal memperbarui alamat", Toast.LENGTH_SHORT).show());
        } else {
            // --- MODE TAMBAH (CREATE) ---
            address.put("isDefault", false); // Alamat baru tidak default

            db.collection("users").document(currentUser.getUid()).collection("addresses")
                    .add(address)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(AddAddressActivity.this, "Alamat baru berhasil disimpan", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(AddAddressActivity.this, "Gagal menyimpan alamat", Toast.LENGTH_SHORT).show());
        }
    }
}