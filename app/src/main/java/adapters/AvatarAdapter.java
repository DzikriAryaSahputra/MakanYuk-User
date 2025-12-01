package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deliveryfood.R;

import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder> {

    private Context context;
    private List<String> avatarNameList;
    private OnAvatarListener listener;

    // Interface untuk mengirim data (avatar yang dipilih) kembali ke Activity
    public interface OnAvatarListener {
        void onAvatarClick(String avatarName);
    }

    public AvatarAdapter(Context context, List<String> avatarNameList, OnAvatarListener listener) {
        this.context = context;
        this.avatarNameList = avatarNameList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_avatar, parent, false);
        return new AvatarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarViewHolder holder, int position) {
        String avatarName = avatarNameList.get(position);

        // Mengambil ID gambar dari drawable berdasarkan namanya
        int imageResId = context.getResources().getIdentifier(avatarName, "drawable", context.getPackageName());

        // Mencegah error jika gambar tidak ditemukan
        if (imageResId != 0) {
            holder.avatarImageView.setImageResource(imageResId);
        } else {
            holder.avatarImageView.setImageResource(R.mipmap.ic_launcher_round); // Gambar default
        }

        // Menambahkan listener klik pada setiap avatar
        holder.itemView.setOnClickListener(v -> {
            listener.onAvatarClick(avatarName);
        });
    }

    @Override
    public int getItemCount() {
        return avatarNameList.size();
    }

    // ViewHolder untuk menampung view item_avatar.xml
    public static class AvatarViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatarImageView;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
        }
    }
}