package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deliveryfood.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import models.CartItem;

// Adapter ini menggunakan CartItem.java sebagai model datanya
public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private Context context;
    private List<CartItem> cartItemList;

    public CheckoutAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        holder.itemName.setText(item.getFoodName());

        // Format kuantitas "1x"
        holder.itemQuantity.setText(item.getQuantity() + "x");

        // Format harga
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        holder.itemPrice.setText(formatRupiah.format(item.getPrice() * item.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        TextView itemQuantity, itemName, itemPrice;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            itemQuantity = itemView.findViewById(R.id.checkoutItemQuantity);
            itemName = itemView.findViewById(R.id.checkoutItemName);
            itemPrice = itemView.findViewById(R.id.checkoutItemPrice);
        }
    }
}