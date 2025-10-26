package com.example.deliveryfood;

import android.content.Context;
import android.content.Intent; // <-- TAMBAHKAN IMPORT INI
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PilihanHariIniAdapter extends RecyclerView.Adapter<PilihanHariIniAdapter.ViewHolder> {

    private Context context;
    private List<PilihanHariIni> listPilihan;

    public PilihanHariIniAdapter(Context context, List<PilihanHariIni> listPilihan) {
        this.context = context;
        this.listPilihan = listPilihan;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pilihan_hari_ini, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PilihanHariIni pilihan = listPilihan.get(position);
        holder.textViewPilihan.setText(pilihan.getName());
        Glide.with(context).load(pilihan.getImageUrl()).into(holder.imageViewPilihan);

        // === BAGIAN TERPENTING: MENAMBAHKAN ONCLICKLISTENER ===
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buat Intent untuk membuka DetailActivity
                Intent intent = new Intent(context, DetailActivity.class);

                // Kirim ID produk yang unik dari item yang diklik
                intent.putExtra("PRODUCT_ID", pilihan.getProductId());

                // Jalankan Intent untuk berpindah halaman
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listPilihan.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPilihan;
        TextView textViewPilihan;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPilihan = itemView.findViewById(R.id.imageViewPilihan);
            textViewPilihan = itemView.findViewById(R.id.textViewPilihan);
        }
    }
}