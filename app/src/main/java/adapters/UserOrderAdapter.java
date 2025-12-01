package adapters; // Sesuaikan package user kamu

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deliveryfood.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import models.CartItem;
import models.Order;

public class UserOrderAdapter extends RecyclerView.Adapter<UserOrderAdapter.ViewHolder> {

    private Context context;
    private List<Order> list;

    // Listener untuk klik item (masuk ke detail nanti)
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(Order order);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public UserOrderAdapter(Context context, List<Order> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = list.get(position);

        // 1. Tanggal
        if (order.getOrderTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            holder.tvDate.setText(sdf.format(order.getOrderTimestamp()));
        }

        // 2. Items (Ringkasan)
        StringBuilder items = new StringBuilder();
        if (order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                items.append(item.getQuantity()).append("x ").append(item.getFoodName()).append(", ");
            }
        }
        // Hapus koma terakhir
        if (items.length() > 2) items.setLength(items.length() - 2);
        holder.tvItems.setText(items.toString());

        // 3. Total Harga
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        holder.tvTotal.setText(formatRupiah.format(order.getTotal()));

        // 4. Status & Warna
        String status = order.getStatus();
        holder.tvStatus.setText(status);

        // Logika Warna Status User
        int colorBg, colorText;
        switch (status) {
            case "Selesai":
                colorBg = 0xFFE8F5E9; // Hijau Muda
                colorText = 0xFF2E7D32; // Hijau Tua
                break;
            case "Dibatalkan":
                colorBg = 0xFFFFEBEE; // Merah Muda
                colorText = 0xFFC62828; // Merah Tua
                break;
            case "Sedang Diantar":
                colorBg = 0xFFE3F2FD; // Biru Muda
                colorText = 0xFF1565C0; // Biru Tua
                break;
            default: // Menunggu Konfirmasi / Dimasak / Pembayaran
                colorBg = 0xFFFFF3E0; // Oranye Muda
                colorText = 0xFFEF6C00; // Oranye Tua
                break;
        }
        holder.tvStatus.setBackgroundColor(colorBg);
        holder.tvStatus.setTextColor(colorText);

        // 5. Klik
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(order);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStatus, tvItems, tvTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvItems = itemView.findViewById(R.id.tvOrderItems);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
        }
    }
}