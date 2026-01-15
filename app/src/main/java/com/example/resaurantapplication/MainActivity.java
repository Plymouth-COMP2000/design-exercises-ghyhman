package com.example.resaurantapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
public class MainActivity extends AppCompatActivity {
    private EditText userBox, passBox;
    private Button btnLogin, btnRegister;
    private TextView errorText;
    private RequestQueue volleyQ;
    private static final String API_BASE = "http://10.240.72.69/comp2000/coursework/";
    private static final String sid = "10894247";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userBox = findViewById(R.id.username_input);
        passBox = findViewById(R.id.Password_input);
        btnLogin = findViewById(R.id.login_button);
        errorText = findViewById(R.id.login_error_text);
        volleyQ = Volley.newRequestQueue(this);

        btnLogin.setOnClickListener(v -> tryLogin());

        btnRegister = findViewById(R.id.Register_button);
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistrationPage.class);
            startActivity(intent);
        });

        Button togglePass = findViewById(R.id.password_toggle_button);
        togglePass.setOnClickListener(v -> {
            if (passBox.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                passBox.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePass.setText("O");
            } else {
                passBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePass.setText("*");
            }
            passBox.setSelection(passBox.getText().length());
        });
    }

    private void tryLogin() {
        String username = userBox.getText().toString().trim();
        String password = passBox.getText().toString();

        errorText.setVisibility(View.GONE);

        if (username.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.error_enter_details));
            return;
        }

        btnLogin.setEnabled(false);

        try {
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8.toString());
            String url = API_BASE + "read_user/" + sid + "/" + encodedUsername;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        btnLogin.setEnabled(true);
                        try {
                            JSONObject userObj;
                            if (response.has("user") && response.get("user") instanceof JSONObject) {
                                userObj = response.getJSONObject("user");
                            } else {
                                userObj = response;
                            }

                            String apiUsername = userObj.optString("username", "");
                            String apiPassword = userObj.optString("password", "");
                            String userType = userObj.optString("usertype", "");
                            String fullName = (userObj.optString("firstname", "") + " " +
                                    userObj.optString("lastname", "")).trim();

                            if (apiUsername.isEmpty() || apiPassword.isEmpty() || userType.isEmpty()) {
                                showError(getString(R.string.login_failed));
                                return;
                            }

                            if (apiPassword.equals(password)) {
                                handleSuccessfulLogin(apiUsername, userType, fullName);
                            } else {
                                showError(getString(R.string.login_failed));
                            }
                        } catch (JSONException e) {
                            showError(getString(R.string.login_failed));
                        }
                    },
                    error -> {
                        btnLogin.setEnabled(true);
                        handleVolleyError(error);
                    });

            volleyQ.add(request);

        } catch (UnsupportedEncodingException e) {
            btnLogin.setEnabled(true);
            showError(getString(R.string.booking_failed));
        }
    }

    private void handleSuccessfulLogin(String username, String userType, String fullName) {
        SharedPreferences prefsStore = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefsStore.edit();
        editor.putString("session_username", username);
        editor.putString("session_type", userType);
        editor.putString("session_name", fullName);
        editor.apply();

        Intent intent;
        if (userType.equalsIgnoreCase("staff")) {
            intent = new Intent(MainActivity.this, StaffHomePage.class);
        } else {
            intent = new Intent(MainActivity.this, HomePage.class);
        }
        intent.putExtra("user_name", fullName);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void handleVolleyError(VolleyError error) {
        String message = getString(R.string.login_failed);
        if (error.networkResponse == null) {
            message = getString(R.string.network_error);
        }
        showError(message);
    }
}
