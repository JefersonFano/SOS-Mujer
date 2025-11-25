package com.example.sos_mujer;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.sos_mujer.utils.FontScaleHelper;
import com.example.sos_mujer.utils.LocaleHelper;

public class SosMujerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Aplicar idioma guardado
        LocaleHelper.applyLanguage(this);

        // Aplicar modo oscuro guardado
        boolean temaOscuro = getSharedPreferences("preferencias", MODE_PRIVATE)
                .getBoolean("temaOscuro", false);

        if (temaOscuro) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Aplicar tamaño de texto global
        FontScaleHelper.applyFontScale(this);
    }
}
