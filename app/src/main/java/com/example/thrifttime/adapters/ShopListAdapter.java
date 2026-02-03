package com.example.thrifttime.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.thrifttime.R;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;

public class ShopListAdapter extends RecyclerView.Adapter<ShopListAdapter.ViewHolder> {

    // 1. Model Class with isRecommended flag
    public static class ShopItem {
        public String name;
        public String address;
        public String rating;
        public Bitmap image;
        public LatLng latLng;
        public boolean isRecommended;

        public ShopItem(String name, String address, String rating, Bitmap image, LatLng latLng, boolean isRecommended) {
            this.name = name;
            this.address = address;
            this.rating = rating;
            this.image = image;
            this.latLng = latLng;
            this.isRecommended = isRecommended;
        }
    }

    private List<ShopItem> shopList;
    private OnShopClickListener listener;

    public interface OnShopClickListener {
        void onShopClick(ShopItem item);
    }

    public ShopListAdapter(List<ShopItem> shopList, OnShopClickListener listener) {
        this.shopList = shopList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShopItem item = shopList.get(position);

        holder.tvName.setText(item.name);
        holder.tvAddress.setText(item.address);
        holder.tvRating.setText(item.rating);

        if (item.image != null) {
            holder.ivImage.setImageBitmap(item.image);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // 2. Logic to Show/Hide Badge
        if (item.isRecommended) {
            holder.tvBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvBadge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onShopClick(item));
    }

    @Override
    public int getItemCount() { return shopList.size(); }

    // 3. ViewHolder MUST define tvBadge here
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvRating, tvBadge; // <--- Declare it here
        ImageView ivImage;

        public ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvShopCardTitle);
            tvAddress = v.findViewById(R.id.tvShopCardAddress);
            tvRating = v.findViewById(R.id.tvShopCardRating);
            ivImage = v.findViewById(R.id.ivShopCardImage);

            // <--- Find it here. This line fixes the error.
            tvBadge = v.findViewById(R.id.tvBadge);
        }
    }
}