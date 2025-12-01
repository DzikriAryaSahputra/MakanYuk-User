package adapters;

import android.content.Context;
import android.content.Intent; // <-- PASTIKAN IMPORT INI ADA
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import activities.DetailActivity;
import com.example.deliveryfood.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import models.Menu;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private Context context;
    private List<Menu> menuList;

    public MenuAdapter(Context context, List<Menu> menuList) {
        this.context = context;
        this.menuList = menuList;
    }
    public void filterList(List<Menu> filteredList) {
        this.menuList = filteredList;
        notifyDataSetChanged(); // Perbarui tampilan RecyclerView
    }
    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_popular_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Menu menu = menuList.get(position);

        // Mengikat data (kode Anda yang sudah ada)
        holder.textViewMenuName.setText(menu.getName());
        holder.textViewMenuDescription.setText(menu.getDescription());
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        holder.textViewPrice.setText(formatRupiah.format(menu.getPrice()));
        String ratingInfo = menu.getRating() + " • " + menu.getEstimation();
        holder.textViewRating.setText(ratingInfo);
        Glide.with(context).load(menu.getImageUrl()).into(holder.imageViewMenu);

        // === BAGIAN TERPENTING: MENAMBAHKAN ONCLICKLISTENER ===
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Buat Intent untuk membuka DetailActivity
                Intent intent = new Intent(context, DetailActivity.class);

                // 2. Kirim ID produk yang unik ke DetailActivity
                intent.putExtra("PRODUCT_ID", menu.getProductId());

                // 3. Jalankan Intent untuk berpindah halaman
                context.startActivity(intent);
            }
        });
        // =======================================================
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    // ViewHolder (tidak ada perubahan)
    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRating, textViewMenuName, textViewMenuDescription, textViewPrice;
        ImageView imageViewMenu;
        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            textViewMenuName = itemView.findViewById(R.id.textViewMenuName);
            textViewMenuDescription = itemView.findViewById(R.id.textViewMenuDescription);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            imageViewMenu = itemView.findViewById(R.id.imageViewMenu);
        }
    }
}