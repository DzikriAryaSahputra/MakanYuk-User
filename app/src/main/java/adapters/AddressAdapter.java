package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deliveryfood.R;

import java.util.List;

import models.Address;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private Context context;
    private List<Address> addressList;
    private AddressListener listener;

    // Interface untuk mengirim aksi klik (edit/hapus) kembali ke Activity
    public interface AddressListener {
        void onEditClick(Address address);
        void onDeleteClick(Address address);
        void onSetDefaultClick(Address address); // Untuk memilih alamat utama
    }

    public AddressAdapter(Context context, List<Address> addressList, AddressListener listener) {
        this.context = context;
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);

        holder.addressLabel.setText(address.getLabel());
        holder.recipientName.setText(address.getRecipientName());
        holder.phoneNumber.setText(address.getPhoneNumber());
        holder.fullAddress.setText(address.getFullAddress());

        // Tampilkan/sembunyikan tag "Utama"
        if (address.getIsDefault()) { // <-- Gunakan getIsDefault()
            holder.defaultTag.setVisibility(View.VISIBLE);
        } else {
            holder.defaultTag.setVisibility(View.GONE);
        }

        // Atur listener untuk tombol
        holder.editButton.setOnClickListener(v -> listener.onEditClick(address));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(address));

        // Atur listener untuk klik seluruh item (menjadikan default)
        holder.itemView.setOnClickListener(v -> listener.onSetDefaultClick(address));
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView addressLabel, recipientName, phoneNumber, fullAddress, defaultTag;
        ImageButton editButton, deleteButton;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            addressLabel = itemView.findViewById(R.id.addressLabelTextView);
            recipientName = itemView.findViewById(R.id.addressRecipientName);
            phoneNumber = itemView.findViewById(R.id.addressPhoneNumber);
            fullAddress = itemView.findViewById(R.id.addressFullDetails);
            defaultTag = itemView.findViewById(R.id.defaultAddressTag);
            editButton = itemView.findViewById(R.id.editAddressButton);
            deleteButton = itemView.findViewById(R.id.deleteAddressButton);
        }
    }
}