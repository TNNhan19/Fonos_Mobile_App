package com.example.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;       // ← thêm dòng này
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = LocaleHelper.getSavedLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FonosDatabaseHelper dbHelper = new FonosDatabaseHelper(this);

        Button btnLogin       = findViewById(R.id.btnLogin);
        EditText etUsername   = findViewById(R.id.etUsername);  // ← thêm dòng này
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);
        Button btnVI          = findViewById(R.id.btnVietnamese);
        Button btnEN          = findViewById(R.id.btnEnglish);

        EditText etPassword = findViewById(R.id.etPassword);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_info), Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = dbHelper.loginUser(username, password);

            if (success) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            } else {
                Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
            }
        });

        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        btnVI.setOnClickListener(v -> changeLanguage("vi"));
        btnEN.setOnClickListener(v -> changeLanguage("en"));
    }

    private void changeLanguage(String lang) {
        LocaleHelper.saveLanguage(this, lang);
        recreate();
    }
}