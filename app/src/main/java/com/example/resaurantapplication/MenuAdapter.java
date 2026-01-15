package com.example.resaurantapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private ArrayList<MenuItem> menuData;

    public MenuAdapter(ArrayList<MenuItem> menuItems) {
        this.menuData = menuItems;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item_row, parent, false);
        return new MenuViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = menuData.get(position);
        holder.nameText.setText(item.itemName);
        holder.priceText.setText(String.format("£%.2f", item.itemPrice));
        holder.descriptionText.setText(item.itemDesc);
        holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);

        if (item.imgPath != null && !item.imgPath.isEmpty()) {
            File imgFile = new File(item.imgPath);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if (myBitmap != null) {
                    holder.imageView.setImageBitmap(myBitmap);
                } else {
                    Log.e("MenuAdapter", "Failed to decode: " + item.imgPath);
                }
            } else {
                Log.w("MenuAdapter", "File not found: " + item.imgPath);
            }
        }
    }

    @Override
    public int getItemCount() {
        return menuData.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, priceText, descriptionText;
        ImageView imageView;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.row_item_name);
            priceText = itemView.findViewById(R.id.row_item_price);
            descriptionText = itemView.findViewById(R.id.row_item_description);
            imageView = itemView.findViewById(R.id.row_item_image);
        }
    }
}
