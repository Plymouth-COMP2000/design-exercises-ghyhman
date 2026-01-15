package com.example.resaurantapplication;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
public class MenuActivity extends AppCompatActivity {

    private RecyclerView menuList;
    private MenuAdapter menuAdapter;
    private DB dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        ImageView btnBack = findViewById(R.id.back_arrow);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        dbHelper = new DB(this);
        ArrayList<MenuItem> menuData = dbHelper.fetchMenu();

        menuList = findViewById(R.id.menu_recycler_view);
        menuList.setLayoutManager(new LinearLayoutManager(this));
        
        menuAdapter = new MenuAdapter(menuData);
        menuList.setAdapter(menuAdapter);
    }
}
