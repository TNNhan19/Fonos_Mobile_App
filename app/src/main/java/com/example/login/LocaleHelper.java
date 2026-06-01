package com.example.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import java.util.Locale;

public class LocaleHelper {

    public static Context setLocale(Context context, String language) {
        saveLanguage(context, language);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }

    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return prefs.getString("language", "en");
    }

    public static void saveLanguage(Context context, String language) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit()
                .putString("language", language)
                .apply();
    }
}