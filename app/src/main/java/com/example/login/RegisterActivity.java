package com.example.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = LocaleHelper.getSavedLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etFullname = findViewById(R.id.etFullname);
        EditText etUsername = findViewById(R.id.etUsername);
        EditText etEmail    = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button   btnRegister = findViewById(R.id.btnRegister);
        TextView tvBack      = findViewById(R.id.tvBack);
        TextView tvGoLogin   = findViewById(R.id.tvGoLogin);
        Button   btnVI       = findViewById(R.id.btnVietnamese);
        Button   btnEN       = findViewById(R.id.btnEnglish);

        btnRegister.setOnClickListener(v -> {
            String name  = etFullname.getText().toString().trim();
            String user  = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(user)
                    || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, getString(R.string.fill_all_info), Toast.LENGTH_SHORT).show();
                return;
            }
            FonosDatabaseHelper dbHelper = new FonosDatabaseHelper(this);

            boolean success = dbHelper.registerUser(name, user, email, pass);

            if (success) {
                Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, getString(R.string.username_exists), Toast.LENGTH_SHORT).show();
            }
        });

        tvBack.setOnClickListener(v -> finish());
        tvGoLogin.setOnClickListener(v -> finish());

        btnVI.setOnClickListener(v -> changeLanguage("vi"));
        btnEN.setOnClickListener(v -> changeLanguage("en"));
    }

    private void changeLanguage(String lang) {
        LocaleHelper.saveLanguage(this, lang);
        recreate();
    }
}