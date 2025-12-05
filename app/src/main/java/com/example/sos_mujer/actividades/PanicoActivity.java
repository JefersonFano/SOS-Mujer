package com.example.sos_mujer.actividades;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sos_mujer.R;
import com.example.sos_mujer.servicios.LocationUpdatesService;
import com.example.sos_mujer.sqlite.SosMujerSqlite;
import com.example.sos_mujer.utils.EmergenciaManager;
import com.example.sos_mujer.utils.LanguageHelper;
import com.example.sos_mujer.utils.FontScaleHelper;

import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class PanicoActivity extends AppCompatActivity {

    private static final String URL_CONTACTOS =
            "http://sos-mujer.atwebpages.com/ws/mostrarContacto.php?usuario_id=";

    private static final String URL_INICIAR =
            "http://sos-mujer.atwebpages.com/ws/iniciar_emergencia.php";

    private static final String URL_FINALIZAR =
            "http://sos-mujer.atwebpages.com/ws/finalizar_emergencia.php";

    private Button btnPanico, btnDetener;

    private int usuarioId;
    private SosMujerSqlite db;

    // Doble pulsación botón físico
    private static final int MAX_PRESIONES = 3;
    private int contadorPresiones = 0;
    private long tiempoUltimaPresion = 0;
    private static final long TIEMPO_MAXIMO = 1000;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FontScaleHelper.applyFontScale(this);
        setContentView(R.layout.activity_panico);

        btnPanico = findViewById(R.id.btnPanico);
        btnDetener = findViewById(R.id.btnDetener);

        db = new SosMujerSqlite(this);
        usuarioId = db.getUsuarioId();

        if (usuarioId == -1) {
            Toast.makeText(this, "Error: no hay usuario registrado", Toast.LENGTH_LONG).show();
            return;
        }

        solicitarAppSmsPredeterminada();
        verificarPermisos();

        btnPanico.setOnClickListener(v -> iniciarEmergenciaEnServidor());
        btnDetener.setOnClickListener(v -> mostrarDialogoPassword());
    }

    // ================
    // SOLICITAR APP SMS
    // ================
    private void solicitarAppSmsPredeterminada() {
        try {
            String actual = Telephony.Sms.getDefaultSmsPackage(this);
            if (actual == null || !actual.equals(getPackageName())) {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
                startActivity(intent);
            }
        } catch (Exception ignored) {}
    }

    // =====================
    // PERMISOS NECESARIOS
    // =====================
    private void verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    101);
        }
    }

    // ======================================================
    //   ➤ INICIAR EMERGENCIA: obtiene emergencia_id del WS
    // ======================================================
    private void iniciarEmergenciaEnServidor() {

        if (EmergenciaManager.esEmergenciaActiva(this)) {
            Toast.makeText(this, "Ya tienes una emergencia activa", Toast.LENGTH_SHORT).show();
            return;
        }

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("usuario_id", usuarioId);

        client.post(URL_INICIAR, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    if (response.getBoolean("ok")) {
                        int emergenciaId = response.getInt("emergencia_id");

                        EmergenciaManager.activarEmergencia(PanicoActivity.this, emergenciaId);

                        obtenerUbicacionYEnviarMensajes(false); // Solo enviamos SMS
                        iniciarServicioUbicacionEnVivo();       // Enviar ubicaciones continuas
                    }
                } catch (Exception e) {
                    Log.e("PANICO", "Error iniciando emergencia", e);
                }
            }
        });
    }

    // ==========================================
    //   ENVÍO DE SMS + OBTENER DIRECCIÓN
    // ==========================================
    private String obtenerDireccion(double lat, double lon) {
        try {
            Geocoder g = new Geocoder(this, Locale.getDefault());
            List<Address> list = g.getFromLocation(lat, lon, 1);

            if (list != null && !list.isEmpty()) {
                return list.get(0).getAddressLine(0);
            }
        } catch (Exception ignored) {}

        return "Lat: " + lat + ", Lon: " + lon;
    }

    private void obtenerUbicacionYEnviarMensajes(boolean iniciarServicio) {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.getFusedLocationProviderClient(this)
                .getLastLocation().addOnSuccessListener(location -> {

                    if (location == null) return;

                    String direccion = obtenerDireccion(location.getLatitude(), location.getLongitude());
                    String trackingUrl = "http://sos-mujer.atwebpages.com/ws/live_location.php?usuario_id=" + usuarioId;

                    enviarMensajes(direccion, trackingUrl);
                });
    }

    private void enviarMensajes(String direccion, String trackingUrl) {

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(URL_CONTACTOS + usuarioId, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int status, Header[] h, JSONArray contacts) {

                if (contacts.length() == 0) {
                    Toast.makeText(PanicoActivity.this,
                            "No hay contactos registrados", Toast.LENGTH_SHORT).show();
                    return;
                }

                String msg = "¡EMERGENCIA! Necesito ayuda urgente.\n"
                        + "Ubicación: " + direccion + "\n"
                        + "Ubicación en vivo: " + trackingUrl + "\n"
                        + "SOS Mujer";

                SmsManager sms = SmsManager.getDefault();

                for (int i = 0; i < contacts.length(); i++) {
                    try {
                        JSONObject c = contacts.getJSONObject(i);
                        String numero = c.getString("numero");

                        if (numero.length() == 9 && !numero.startsWith("+51"))
                            numero = "+51" + numero;

                        ArrayList<String> partes = sms.divideMessage(msg);
                        sms.sendMultipartTextMessage(numero, null, partes, null, null);

                    } catch (Exception e) {
                        Log.e("SMS", "Error enviando SMS", e);
                    }
                }

                Toast.makeText(PanicoActivity.this,
                        "Mensajes enviados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ============================
    //   INICIAR SERVICIO DE GPS
    // ============================
    private void iniciarServicioUbicacionEnVivo() {
        Intent i = new Intent(this, LocationUpdatesService.class);
        i.putExtra("usuario_id", usuarioId);
        ContextCompat.startForegroundService(this, i);
    }

    // =============================
    //   DETENER LA EMERGENCIA
    // =============================
    private void mostrarDialogoPassword() {

        if (!EmergenciaManager.esEmergenciaActiva(this)) {
            Toast.makeText(this, "No hay emergencia activa", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText input = new EditText(this);
        input.setHint("Contraseña");
        input.setInputType(129);

        new AlertDialog.Builder(this)
                .setTitle("Detener emergencia")
                .setMessage("Ingresa tu contraseña para detener la emergencia")
                .setView(input)
                .setPositiveButton("Aceptar", (dialog, which) -> {

                    String passIngresada = input.getText().toString().trim();
                    String hashGuardado = db.getPassword();
                    String hashIngresada = sha256(passIngresada);

                    if (!hashIngresada.equals(hashGuardado)) {
                        Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    detenerEmergencia();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void detenerEmergencia() {

        int emergenciaId = EmergenciaManager.getEmergenciaId(this);

        // Detener servicio
        stopService(new Intent(this, LocationUpdatesService.class));

        // Notificar al servidor
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams p = new RequestParams();
        p.put("emergencia_id", emergenciaId);

        client.post(URL_FINALIZAR, p, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int s, Header[] h, JSONObject r) {
                Toast.makeText(PanicoActivity.this, "Emergencia finalizada", Toast.LENGTH_SHORT).show();
            }
        });

        EmergenciaManager.desactivarEmergencia(this);
    }

    // ====================
    // SHA-256
    // ====================
    private String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes());
            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                String h = Integer.toHexString(0xFF & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();

        } catch (NoSuchAlgorithmException ex) {
            return "";
        }
    }

    // ====================================================
    // ATAJO: 3 PRESIONES DE BOTÓN VOLUMEN ABAJO
    // ====================================================
    @Override
    public boolean onKeyDown(int key, KeyEvent e) {
        if (key == KeyEvent.KEYCODE_VOLUME_DOWN) {

            long t = System.currentTimeMillis();

            if (t - tiempoUltimaPresion < TIEMPO_MAXIMO)
                contadorPresiones++;
            else
                contadorPresiones = 1;

            tiempoUltimaPresion = t;

            if (contadorPresiones >= MAX_PRESIONES) {
                iniciarEmergenciaEnServidor();
                contadorPresiones = 0;
                return true;
            }
        }
        return super.onKeyDown(key, e);
    }
}
