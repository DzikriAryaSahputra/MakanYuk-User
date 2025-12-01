package fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import activities.DetailOrderActivity;
import com.example.deliveryfood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import adapters.UserOrderAdapter;
import models.Order;

public class UserOrderFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private UserOrderAdapter adapter;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private View loadingOverlay; // Variabel Loading
    private String currentUserId;
    private String filterType;

    private static final String TAG = "UserOrderFragment";

    public UserOrderFragment() { }

    public static UserOrderFragment newInstance(String filterType) {
        UserOrderFragment fragment = new UserOrderFragment();
        Bundle args = new Bundle();
        args.putString("FILTER", filterType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filterType = getArguments().getString("FILTER");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_orders, container, false);

        // 1. INISIALISASI VIEW DULU (PENTING)
        // Jangan panggil setVisibility sebelum baris ini!
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        recyclerView = view.findViewById(R.id.recyclerUserOrders);
        tvEmpty = view.findViewById(R.id.tvEmptyOrders);

        // 2. BARU TAMPILKAN LOADING
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();

        adapter = new UserOrderAdapter(getContext(), orderList);

        // Listener klik untuk masuk ke detail
        adapter.setOnItemClickListener(order -> {
            Intent intent = new Intent(getContext(), DetailOrderActivity.class);
            intent.putExtra("ORDER_DATA", order);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // Panggil data (Loading akan hilang di dalam method ini jika data sudah dapat)
        loadOrders();

        return view;
    }

    private void loadOrders() {
        if (currentUserId == null) return;

        Query query = db.collection("orders")
                .whereEqualTo("userId", currentUserId);

        if ("active".equals(filterType)) {
            List<String> activeStatuses = new ArrayList<>();
            activeStatuses.add("Menunggu Pembayaran");
            activeStatuses.add("Menunggu Konfirmasi");
            activeStatuses.add("Sedang Dimasak");
            activeStatuses.add("Sedang Diantar");
            query = query.whereIn("status", activeStatuses);
        } else {
            List<String> historyStatuses = new ArrayList<>();
            historyStatuses.add("Selesai");
            historyStatuses.add("Dibatalkan");
            query = query.whereIn("status", historyStatuses);
        }

        // Urutkan dari yang terbaru
        query.orderBy("orderTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        // Cek agar tidak crash jika fragment sudah ditutup
                        if (!isAdded() || getContext() == null) return;

                        // 3. SEMBUNYIKAN LOADING DISINI (SETELAH KONEKSI SELESAI)
                        if (loadingOverlay != null) {
                            loadingOverlay.setVisibility(View.GONE);
                        }

                        if (error != null) {
                            Log.e(TAG, "Listen failed.", error);
                            Toast.makeText(getContext(), "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (value != null) {
                            orderList.clear(); // Hapus data lama
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                try {
                                    Order order = doc.toObject(Order.class);
                                    if (order != null) {
                                        order.setOrderId(doc.getId());
                                        orderList.add(order);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing order: " + e.getMessage());
                                }
                            }

                            adapter.notifyDataSetChanged();

                            if (orderList.isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                tvEmpty.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }
}