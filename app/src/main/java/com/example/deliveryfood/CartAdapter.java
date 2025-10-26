package com.example.deliveryfood;

import android.content.Context;
import android.view.LayoutInflater;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItemList;
    // Tambahkan listener untuk update kuantitas (akan kita buat nanti)
    private CartUpdateListener listener;

    // Interface untuk komunikasi update ke Activity
    public interface CartUpdateListener {
        void onQuantityChanged(String productId, int newQuantity);
    }

    public CartAdapter(Context context, List<CartItem> cartItemList, CartUpdateListener listener) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        holder.cartItemName.setText(item.getName());
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        holder.cartItemPrice.setText(formatRupiah.format(item.getPrice()));
        holder.textViewQuantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context).load(item.getImageUrl()).into(holder.cartItemImageView);

        // Fungsi tombol plus
        holder.buttonPlus.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            currentQuantity++;
            item.setQuantity(currentQuantity);
            holder.textViewQuantity.setText(String.valueOf(currentQuantity));
            if (listener != null) {
                listener.onQuantityChanged(item.getProductId(), currentQuantity);
            }
        });

        // Fungsi tombol minus
        holder.buttonMinus.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            if (currentQuantity > 1) {
                currentQuantity--;
                item.setQuantity(currentQuantity);
                holder.textViewQuantity.setText(String.valueOf(currentQuantity));
                if (listener != null) {
                    listener.onQuantityChanged(item.getProductId(), currentQuantity);
                }
            } else if (currentQuantity == 1) {
                // Jika kuantitas 1 dikurangi, item dihapus (kuantitas jadi 0)
                if (listener != null) {
                    listener.onQuantityChanged(item.getProductId(), 0);
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buat Intent untuk membuka DetailActivity
                Intent intent = new Intent(context, DetailActivity.class);
                // Kirim ID produk dari item keranjang yang diklik
                intent.putExtra("PRODUCT_ID", item.getProductId());
                // Jalankan Intent
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView cartItemImageView;
        TextView cartItemName, cartItemPrice, textViewQuantity;
        ImageButton buttonMinus, buttonPlus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cartItemImageView = itemView.findViewById(R.id.cartItemImageView);
            cartItemName = itemView.findViewById(R.id.cartItemName);
            cartItemPrice = itemView.findViewById(R.id.cartItemPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            buttonMinus = itemView.findViewById(R.id.buttonMinus);
            buttonPlus = itemView.findViewById(R.id.buttonPlus);
        }
    }
}