package com.example.resaurantapplication;

import android.graphics.Bitmap;
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

public class AddMenuItems extends AppCompatActivity {

    private EditText nameBox, priceBox, descBox;
    private Button btnAdd, btnPickImg;
    private ImageView imgPreview;
    private DB dbHelper;
    private String imgPath = "";

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imgPath = saveImageToInternalStorage(uri);
                    if (imgPath != null) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                        if (bitmap != null) {
                            imgPreview.setImageBitmap(bitmap);
                        }
                    } else {
                        Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_menu_items);

        dbHelper = new DB(this);

        nameBox = findViewById(R.id.nameBox);
        priceBox = findViewById(R.id.priceBox);
        descBox = findViewById(R.id.descBox);
        btnAdd = findViewById(R.id.btnAdd);
        btnPickImg = findViewById(R.id.btnPickImg);
        imgPreview = findViewById(R.id.imgPreview);

        if (findViewById(R.id.back_arrow) != null) {
            findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
        }

        btnPickImg.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnAdd.setOnClickListener(v -> {
            String name = nameBox.getText().toString().trim();
            String priceStr = priceBox.getText().toString().trim();
            String description = descBox.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(AddMenuItems.this, "Please enter name and price", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                long id = dbHelper.addItemToMenu(name, price, description, imgPath);
                if (id != -1) {
                    Toast.makeText(AddMenuItems.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddMenuItems.this, "Failed to add item", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(AddMenuItems.this, "Invalid price format", Toast.LENGTH_SHORT).show();
            }
        });
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

            Log.d("AddMenuItems", "Image saved to path: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e("AddMenuItems", "Error saving image", e);
            return null;
        }
    }
}
