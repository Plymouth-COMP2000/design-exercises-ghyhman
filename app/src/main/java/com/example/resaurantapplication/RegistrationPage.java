package com.example.resaurantapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public class RegistrationPage extends AppCompatActivity {
    private EditText firstNameInput, surnameInput, usernameInput, emailRealInput, passwordInput, confirmPasswordInput;
    private Button signupButton;
    private RequestQueue requestQueue;
    private final String STUDENT_ID = "10894247";
    private final String BASE_URL = "http://10.240.72.69/comp2000/coursework/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_page);

        firstNameInput = findViewById(R.id.first_name_input);
        surnameInput = findViewById(R.id.surname_input);
        usernameInput = findViewById(R.id.username_input);
        emailRealInput = findViewById(R.id.email_real_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        signupButton = findViewById(R.id.signup_button);
        requestQueue = Volley.newRequestQueue(this);
        signupButton.setOnClickListener(v -> handleRegistration());

        Button alreadyAccountBtn = findViewById(R.id.already_account_button);
        alreadyAccountBtn.setOnClickListener(v -> finish());

        Button togglePass = findViewById(R.id.password_toggle);
        togglePass.setOnClickListener(v -> {
            if (passwordInput.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                passwordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePass.setText("O");
            } else {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePass.setText("*");
            }
            passwordInput.setSelection(passwordInput.getText().length());
        });

        Button toggleConfirmPass = findViewById(R.id.confirm_password_toggle);
        toggleConfirmPass.setOnClickListener(v -> {
            if (confirmPasswordInput.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                confirmPasswordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleConfirmPass.setText("O");
            } else {
                confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleConfirmPass.setText("*");
            }
            confirmPasswordInput.setSelection(confirmPasswordInput.getText().length());
        });
    }

    private void handleRegistration() {
        String firstName = firstNameInput.getText().toString().trim();
        String surname = surnameInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String email = emailRealInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (firstName.isEmpty() || surname.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_all_fields_required), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.contains("@")) {
            Toast.makeText(this, getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, getString(R.string.error_passwords_dont_match), Toast.LENGTH_SHORT).show();
            return;
        }

        signupButton.setEnabled(false);
        checkDuplicateAndRegister(firstName, surname, username, email, password);
    }

    private void checkDuplicateAndRegister(String fName, String lName, String username, String email, String password) {
        String url = BASE_URL + "read_all_users/" + STUDENT_ID;

        JsonObjectRequest checkRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray users = response.getJSONArray("users");
                        boolean exists = false;
                        for (int i = 0; i < users.length(); i++) {
                            if (users.getJSONObject(i).getString("username").equalsIgnoreCase(username)) {
                                exists = true;
                                break;
                            }
                        }

                        if (exists) {
                            signupButton.setEnabled(true);
                            Toast.makeText(RegistrationPage.this, getString(R.string.error_username_taken), Toast.LENGTH_SHORT).show();
                        } else {
                            performPostRequest(fName, lName, username, email, password);
                        }
                    } catch (JSONException e) {
                        signupButton.setEnabled(true);
                        Toast.makeText(RegistrationPage.this, getString(R.string.error_parsing_data), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    signupButton.setEnabled(true);
                    Toast.makeText(RegistrationPage.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(checkRequest);
    }

    private void performPostRequest(String fName, String lName, String username, String email, String password) {
        String url = BASE_URL + "create_user/" + STUDENT_ID;

        JSONObject params = new JSONObject();
        try {
            params.put("username", username);
            params.put("password", password);
            params.put("firstname", fName);
            params.put("lastname", lName);
            params.put("email", email);
            params.put("contact", "0000000000");
            params.put("usertype", "guest");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> {
                    signupButton.setEnabled(true);
                    Toast.makeText(RegistrationPage.this, getString(R.string.registration_success), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegistrationPage.this, MainActivity.class));
                    finish();
                },
                error -> {
                    signupButton.setEnabled(true);

                    if (error.networkResponse != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            Log.e("RegistrationPage", "HTTP Status Code: " + error.networkResponse.statusCode);
                            Log.e("RegistrationPage", "Response Body: " + responseBody);
                        } catch (Exception e) {
                            Log.e("RegistrationPage", "Error parsing response body", e);
                        }
                    }

                    Toast.makeText(RegistrationPage.this, getString(R.string.registration_failed), Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(postRequest);
    }
}
