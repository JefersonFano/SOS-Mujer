package com.example.sos_mujer.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class EmergenciaManager {

    private static final String PREF = "EMERGENCIA_PREFS";

    public static void activarEmergencia(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putBoolean("emergencia_activa", true).apply();
    }

    public static void desactivarEmergencia(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putBoolean("emergencia_activa", false).apply();
    }

    public static boolean esEmergenciaActiva(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getBoolean("emergencia_activa", false);
    }
}
