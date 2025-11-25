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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

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

    private static final String BASE_URL_LIVE_LOCATION =
            "http://sos-mujer.atwebpages.com/ws/live_location.php?usuario_id=";

    private static final String URL_DETENER =
            "http://sos-mujer.atwebpages.com/ws/detener_emergencia.php?usuario_id=";

    private Button btnPanico, btnDetener;

    private FusedLocationProviderClient fusedLocationClient;
    private int usuarioId;

    private static final int MAX_PRESIONES = 3;
    private int contadorPresiones = 0;
    private long tiempoUltimaPresion = 0;
    private static final long TIEMPO_MAXIMO = 1000;

    private static final int REQ_LOC = 100;
    private static final int REQ_SMS = 101;

    private SosMujerSqlite db;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.sos_mujer.utils.FontScaleHelper.applyFontScale(this);
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnPanico.setOnClickListener(v -> dispararEmergencia());
        btnDetener.setOnClickListener(v -> mostrarDialogoPassword());
    }

    // ==========================
    //   APP SMS POR DEFECTO
    // ==========================
    private void solicitarAppSmsPredeterminada() {
        try {
            String actual = Telephony.Sms.getDefaultSmsPackage(this);
            if (actual == null || !actual.equals(getPackageName())) {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e("SMS_DEFAULT", "No se pudo establecer como app SMS por defecto", e);
        }
    }

    private void verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOC);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQ_SMS);
        }
    }

    // ==========================
    //    INICIAR EMERGENCIA
    // ==========================
    private void dispararEmergencia() {

        if (EmergenciaManager.esEmergenciaActiva(this)) {
            Toast.makeText(this, "Ya tienes una emergencia activa", Toast.LENGTH_SHORT).show();
            return;
        }

        EmergenciaManager.activarEmergencia(this);
        obtenerUbicacionYEnviarMensajes(true);
    }

    private void obtenerUbicacionYEnviarMensajes(boolean iniciarServicio) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Sin permiso de ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location == null) {
                Toast.makeText(this,
                        "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show();
                return;
            }

            double lat = location.getLatitude();
            double lon = location.getLongitude();

            String direccion = obtenerDireccion(lat, lon);
            String trackingUrl = BASE_URL_LIVE_LOCATION + usuarioId;

            enviarMensajes(direccion, trackingUrl);

            if (iniciarServicio) iniciarServicioUbicacionEnVivo();
        });
    }

    private String obtenerDireccion(double lat, double lon) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(lat, lon, 1);

            if (list != null && !list.isEmpty()) {
                return list.get(0).getAddressLine(0);
            }

        } catch (Exception ignored) {}

        return "Lat: " + lat + ", Lon: " + lon;
    }

    private void enviarMensajes(String direccion, String trackingUrl) {

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(URL_CONTACTOS + usuarioId, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray contacts) {

                if (contacts.length() == 0) {
                    Toast.makeText(PanicoActivity.this,
                            "No hay contactos registrados", Toast.LENGTH_SHORT).show();
                    return;
                }

                String mensaje =
                        "¡EMERGENCIA! Necesito ayuda urgente.\n" +
                                "Ubicación: " + direccion + "\n" +
                                "Ubicación en vivo: " + trackingUrl + "\n" +
                                "SOS Mujer";

                for (int i = 0; i < contacts.length(); i++) {
                    try {
                        JSONObject c = contacts.getJSONObject(i);
                        String numero = c.getString("numero");

                        if (numero.length() == 9 && !numero.startsWith("+51")) {
                            numero = "+51" + numero;
                        }

                        enviarSMS(numero, mensaje);

                    } catch (Exception e) {
                        Log.e("SMS", "Error mensaje a contacto", e);
                    }
                }

                Toast.makeText(PanicoActivity.this,
                        "Mensajes enviados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarSMS(String numero, String mensaje) {
        try {
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> partes = sms.divideMessage(mensaje);
            sms.sendMultipartTextMessage(numero, null, partes, null, null);
            Log.d("SMS", "SMS enviado -> " + numero);

        } catch (Exception e) {
            Log.e("SMS", "ERROR SMS", e);
        }
    }

    private void iniciarServicioUbicacionEnVivo() {
        Intent i = new Intent(this, LocationUpdatesService.class);
        i.putExtra("usuario_id", usuarioId);
        ContextCompat.startForegroundService(this, i);
    }

    // ==========================
    //     DETENER EMERGENCIA
    // ==========================
    private void mostrarDialogoPassword() {

        if (!EmergenciaManager.esEmergenciaActiva(this)) {
            Toast.makeText(this, "No hay una emergencia activa", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText input = new EditText(this);
        input.setHint("Contraseña");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Detener emergencia")
                .setMessage("Ingresa tu contraseña para detener la emergencia")
                .setView(input)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    String passIngresada = input.getText().toString().trim();
                    if (passIngresada.isEmpty()) {
                        Toast.makeText(this, "Contraseña vacía", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String hashGuardado = db.getPassword(); // SHA-256 guardado
                    String hashIngresada = sha256(passIngresada);

                    if (!hashIngresada.equals(hashGuardado)) {
                        Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Contraseña OK → detener emergencia
                    detenerEmergencia();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void detenerEmergencia() {

        // Detener servicio
        Intent i = new Intent(this, LocationUpdatesService.class);
        stopService(i);

        // Notificar al servidor (set activo = 0)
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(URL_DETENER + usuarioId, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(PanicoActivity.this,
                        "Emergencia finalizada", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject e) {
                Toast.makeText(PanicoActivity.this,
                        "No se pudo notificar al servidor, inténtalo luego",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Limpiar estado local
        EmergenciaManager.desactivarEmergencia(this);
    }

    // ==========================
    //      SHA-256
    // ==========================
    private String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String h = Integer.toHexString(0xFF & b);
                if (h.length() == 1) hexString.append('0');
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ex) {
            return "";
        }
    }

    // =================================
    // ATAJO: 3 VECES VOLUMEN ABAJO
    // =================================
    @Override
    public boolean onKeyDown(int key, KeyEvent e) {
        if (key == KeyEvent.KEYCODE_VOLUME_DOWN) {
            long t = System.currentTimeMillis();

            if (t - tiempoUltimaPresion < TIEMPO_MAXIMO) contadorPresiones++;
            else contadorPresiones = 1;

            tiempoUltimaPresion = t;

            if (contadorPresiones >= MAX_PRESIONES) {
                dispararEmergencia();
                contadorPresiones = 0;
                return true;
            }
        }
        return super.onKeyDown(key, e);
    }
}
