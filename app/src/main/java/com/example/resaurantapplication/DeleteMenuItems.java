package com.example.resaurantapplication;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DeleteMenuItems extends AppCompatActivity {

    private EditText searchInput, nameOutput;
    private ImageView imgPreview;
    private Switch deleteSwitch;
    private Button searchButton, btnDelete;
    private DB dbHelper;
    private long pickedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_menu_items);

        dbHelper = new DB(this);
        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);
        nameOutput = findViewById(R.id.name_output);
        imgPreview = findViewById(R.id.image_preview);
        deleteSwitch = findViewById(R.id.delete_switch);
        btnDelete = findViewById(R.id.delete_button);
        if (findViewById(R.id.back_arrow) != null) {
            findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
        }
        searchButton.setOnClickListener(v -> {
            String searchText = searchInput.getText().toString().trim();
            if (searchText.isEmpty()) {
                Toast.makeText(this, "Enter a name to search!", Toast.LENGTH_SHORT).show();
                return;
            }

            MenuItem item = dbHelper.getMenuItemByName(searchText);
            if (item != null) {
                pickedId = item.itemId;
                nameOutput.setText(item.itemName);
                if (item.imgPath != null && !item.imgPath.isEmpty()) {
                    try {
                        imgPreview.setImageURI(Uri.parse(item.imgPath));
                    } catch (Exception e) {
                        imgPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } else {
                    imgPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                }

                deleteSwitch.setChecked(false);
                btnDelete.setEnabled(false);
            } else {
                Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                reloadMenuList();
            }
        });
        deleteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnDelete.setEnabled(isChecked && pickedId != -1);
        });

        btnDelete.setOnClickListener(v -> {
            if (pickedId != -1 && deleteSwitch.isChecked()) {
                int rows = dbHelper.removeMenuItem(pickedId);
                if (rows > 0) {
                    Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                    reloadMenuList();
                    searchInput.setText("");
                } else {
                    Toast.makeText(this, "Deletion unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void reloadMenuList() {
        pickedId = -1;
        nameOutput.setText("");
        imgPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        deleteSwitch.setChecked(false);
        btnDelete.setEnabled(false);
    }
}
