package com.example.sos_mujer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

public class FontScaleHelper {

    private static final String PREFS = "preferencias";

    public static void applyFontScale(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            int index = prefs.getInt("tamanoTextoIndex", 1);

            float scale;
            switch (index) {
                case 0: scale = 0.85f; break; // Pequeño
                case 2: scale = 1.15f; break; // Grande
                case 3: scale = 1.30f; break; // Muy grande
                default: scale = 1.0f; break; // Normal
            }

            Resources res = context.getResources();
            Configuration config = res.getConfiguration();

            if (config.fontScale != scale) {
                config.fontScale = scale;
                res.updateConfiguration(config, res.getDisplayMetrics());
            }

        } catch (Exception ignored) { }
    }
}
