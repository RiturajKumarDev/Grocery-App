package com.rituraj.shivamkiranastore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.Models.AddressModel;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<AddressModel> addressList;
    private int selectedPosition = -1;
    private OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onEditClick(AddressModel address, int position);

        void onDeleteClick(AddressModel address, int position);

        void onSetDefaultClick(AddressModel address, int position);

        void onAddressSelected(AddressModel address, int position);
    }

    public AddressAdapter(List<AddressModel> addressList, OnAddressActionListener listener) {
        this.addressList = addressList;
        this.listener = listener;

        // Find default address position if exists
        for (int i = 0; i < addressList.size(); i++) {
            if (addressList.get(i).getType().equals("DEFAULT")) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        AddressModel address = addressList.get(position);

        holder.tvName.setText(address.getFullName());
        holder.tvPhone.setText(address.getPhone());

        // Build full address string
        String fullAddress = address.getAddressLine1();
        if (address.getAddressLine2() != null && !address.getAddressLine2().isEmpty()) {
            fullAddress += ", " + address.getAddressLine2();
        }
        fullAddress += ", " + address.getCity() + ", " + address.getState() + " - " + address.getPincode();
        holder.tvAddress.setText(fullAddress);

        // Set address type badge
        if (address.getType() != null && !address.getType().isEmpty()) {
            holder.tvAddressType.setVisibility(View.VISIBLE);
            holder.tvAddressType.setText(address.getType());
        } else {
            holder.tvAddressType.setVisibility(View.GONE);
        }

        // Show default badge if this is the default address
        if (position == selectedPosition) {
            holder.tvDefaultBadge.setVisibility(View.VISIBLE);
            holder.ivSelectedIndicator.setImageResource(R.drawable.ic_radio_checked);
        } else {
            holder.tvDefaultBadge.setVisibility(View.GONE);
            holder.ivSelectedIndicator.setImageResource(R.drawable.ic_radio_unchecked);
        }

        // Handle item click for selection
        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            if (listener != null) {
                listener.onAddressSelected(address, selectedPosition);
            }

            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
        });

        // Handle action buttons
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(address, position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(address, position);
            }
        });

        holder.btnSetDefault.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = position;

            // Update address type to DEFAULT
            address.setType("DEFAULT");

            if (listener != null) {
                listener.onSetDefaultClick(address, position);
            }

            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public AddressModel getSelectedAddress() {
        if (selectedPosition != -1 && selectedPosition < addressList.size()) {
            return addressList.get(selectedPosition);
        }
        return null;
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAddress, tvAddressType, tvDefaultBadge;
        TextView btnEdit, btnDelete, btnSetDefault;
        ImageView ivSelectedIndicator;

        AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSelectedIndicator = itemView.findViewById(R.id.ivSelectedIndicator);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvAddressType = itemView.findViewById(R.id.tvAddressType);
            tvDefaultBadge = itemView.findViewById(R.id.tvDefaultBadge);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnSetDefault = itemView.findViewById(R.id.btnSetDefault);
        }
    }
}
