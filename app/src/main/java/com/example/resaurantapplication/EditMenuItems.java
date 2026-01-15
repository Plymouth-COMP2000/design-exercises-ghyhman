package com.example.resaurantapplication;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class EditMenuItems extends AppCompatActivity {

    private EditText searchInput, nameEditBox, priceEditBox, descEditBox;
    private ImageView editImagePreview;
    private Button searchButton, uploadImageBtn, btnSave;
    private DB dbHelper;

    private long pickedId = -1;
    private String newSavedImagePath = "";
    private String existingImagePath = "";

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    newSavedImagePath = saveImageToInternalStorage(uri);
                    if (newSavedImagePath != null) {
                        editImagePreview.setImageBitmap(BitmapFactory.decodeFile(newSavedImagePath));
                    } else {
                        Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_menu_items);
        dbHelper = new DB(this);
        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);
        editImagePreview = findViewById(R.id.edit_image_preview);
        uploadImageBtn = findViewById(R.id.upload_new_image_btn);
        nameEditBox = findViewById(R.id.edit_item_name);
        priceEditBox = findViewById(R.id.edit_price);
        descEditBox = findViewById(R.id.edit_description);
        btnSave = findViewById(R.id.confirm_edit_btn);

        if (findViewById(R.id.back_arrow) != null) {
            findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
        }
        searchButton.setOnClickListener(v -> {
            String searchText = searchInput.getText().toString().trim();
            if (searchText.isEmpty()) {
                Toast.makeText(this, "Enter a name to search", Toast.LENGTH_SHORT).show();
                return;
            }

            MenuItem item = dbHelper.getMenuItemByName(searchText);
            if (item != null) {
                pickedId = item.itemId;
                nameEditBox.setText(item.itemName);
                priceEditBox.setText(String.valueOf(item.itemPrice));
                descEditBox.setText(item.itemDesc);
                
                existingImagePath = item.imgPath;
                newSavedImagePath = "";

                loadImageIntoPreview(existingImagePath);
            } else {
                Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                reloadMenuList();
            }
        });

        uploadImageBtn.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> {
            if (pickedId == -1) {
                Toast.makeText(this, "Search for it first", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = nameEditBox.getText().toString().trim();
            String priceStr = priceEditBox.getText().toString().trim();
            String description = descEditBox.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);

                String imageToSave = newSavedImagePath.isEmpty()
                        ? existingImagePath
                        : newSavedImagePath;

                int rows = dbHelper.saveMenuEdit(pickedId, name, price, description, imageToSave);
                if (rows > 0) {
                    Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImageIntoPreview(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                editImagePreview.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
            } else {
                editImagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            editImagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            File directory = new File(getFilesDir(), "menu_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = UUID.randomUUID().toString() + ".jpg";
            File file = new File(directory, fileName);

            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            Log.d("EditMenuItems", "Image saved to: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e("EditMenuItems", "Error saving image", e);
            return null;
        }
    }

    private void reloadMenuList() {
        pickedId = -1;
        nameEditBox.setText("");
        priceEditBox.setText("");
        descEditBox.setText("");
        editImagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
        existingImagePath = "";
        newSavedImagePath = "";
    }
}
