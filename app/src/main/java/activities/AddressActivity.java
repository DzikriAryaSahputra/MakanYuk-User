package activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.deliveryfood.R;
import com.google.firebase.firestore.WriteBatch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import adapters.AddressAdapter;
import models.Address;

public class AddressActivity extends AppCompatActivity implements AddressAdapter.AddressListener {

    private RecyclerView addressRecyclerView;
    private AddressAdapter addressAdapter;
    private List<Address> addressList;
    private FloatingActionButton fabAddAddress;
    private ImageButton backButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private static final String TAG = "AddressActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Cek Login
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi UI
        addressRecyclerView = findViewById(R.id.addressRecyclerView);
        fabAddAddress = findViewById(R.id.fabAddAddress);
        backButton = findViewById(R.id.backButtonAddress);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarAddress);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Setup RecyclerView
        addressRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addressList = new ArrayList<>();
        addressAdapter = new AddressAdapter(this, addressList, this);
        addressRecyclerView.setAdapter(addressAdapter);

        // Atur Listener Tombol
        backButton.setOnClickListener(v -> finish());
        fabAddAddress.setOnClickListener(v -> {
            // Buka halaman AddAddressActivity
            Intent intent = new Intent(AddressActivity.this, AddAddressActivity.class);
            startActivity(intent);
        });

        // Muat alamat dari Firestore
        loadAddresses();
    }

    private void loadAddresses() {
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("addresses")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        addressList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Address address = document.toObject(Address.class);
                            address.setAddressId(document.getId()); // Simpan ID dokumen
                            addressList.add(address);
                        }
                        addressAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Gagal memuat alamat.", task.getException());
                        Toast.makeText(AddressActivity.this, "Gagal memuat alamat.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- Implementasi dari Interface AddressListener ---

    @Override
    public void onEditClick(Address address) {
        Intent intent = new Intent(this, AddAddressActivity.class);
        intent.putExtra("ADDRESS_ID_TO_EDIT", address.getAddressId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Address address) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Alamat")
                .setMessage("Yakin ingin menghapus alamat \"" + address.getLabel() + "\"?")
                .setPositiveButton("Ya, Hapus", (dialog, which) -> {
                    deleteAddressFromFirestore(address);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onSetDefaultClick(Address selectedAddress) {
        if (selectedAddress.getIsDefault()) {
            return;
        }

        Toast.makeText(this, "Mengatur alamat utama...", Toast.LENGTH_SHORT).show();
        String userId = currentUser.getUid();
        WriteBatch batch = db.batch();

        Address oldDefault = null;
        for (Address addr : addressList) {
            if (addr.getIsDefault()) {
                oldDefault = addr;
                break;
            }
        }
        final Address finalOldDefault = oldDefault;
        // -------------------------

        // Gunakan variabel final untuk membangun batch
        if (finalOldDefault != null) {
            batch.update(db.collection("users").document(userId).collection("addresses").document(finalOldDefault.getAddressId()), "isDefault", false);
        }
        batch.update(db.collection("users").document(userId).collection("addresses").document(selectedAddress.getAddressId()), "isDefault", true);

        // Jalankan batch update
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // Gunakan setter baru
                    if (finalOldDefault != null) {
                        finalOldDefault.setIsDefault(false);
                    }
                    selectedAddress.setIsDefault(true);

                    addressAdapter.notifyDataSetChanged();
                    Toast.makeText(this, selectedAddress.getLabel() + " dijadikan alamat utama.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memperbarui alamat.", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error updating default address", e);
                });
    }
    private void deleteAddressFromFirestore(Address address) {
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("addresses").document(address.getAddressId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Alamat berhasil dihapus", Toast.LENGTH_SHORT).show();
                    loadAddresses(); // Muat ulang daftar alamat
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal menghapus alamat", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Gagal menghapus alamat", e);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAddresses();
    }
}