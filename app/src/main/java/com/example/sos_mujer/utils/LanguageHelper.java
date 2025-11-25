package com.example.sos_mujer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class LanguageHelper {

    public static Context applyLanguage(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);

        int pos = preferences.getInt("idioma", 0);

        String lang = "es"; // Español por defecto

        if (pos == 1) lang = "qu";   // Quechua
        else if (pos == 2) lang = "en"; // Inglés

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }
}
