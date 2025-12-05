package com.example.sos_mujer.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class EmergenciaManager {

    private static final String PREF = "EMERGENCIA_PREFS";
    private static final String KEY_ACTIVA = "emergencia_activa";
    private static final String KEY_ID = "emergencia_id";

    // Activar emergencia y guardar ID
    public static void activarEmergencia(Context ctx, int emergenciaId) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putBoolean(KEY_ACTIVA, true)
                .putInt(KEY_ID, emergenciaId)
                .apply();
    }

    // Desactivar emergencia y limpiar ID
    public static void desactivarEmergencia(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putBoolean(KEY_ACTIVA, false)
                .putInt(KEY_ID, -1)
                .apply();
    }

    public static boolean esEmergenciaActiva(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_ACTIVA, false);
    }

    public static int getEmergenciaId(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getInt(KEY_ID, -1);
    }
}
