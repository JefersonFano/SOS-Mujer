package com.example.sos_mujer.actividades;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sos_mujer.R;
import com.example.sos_mujer.sqlite.SosMujerSqlite;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PanicoActivity extends AppCompatActivity {

    private static final String URL_CONTACTOS = "http://sos-mujer.atwebpages.com/ws/mostrarContacto.php?usuario_id=";
    Button btnPanico;
    FusedLocationProviderClient fusedLocationClient;
    double latitud = 0.0;
    double longitud = 0.0;

    // Variables para detectar la triple pulsación
    private static final int MAX_PRESIONES = 3;
    private int contadorPresiones = 0;
    private long tiempoUltimaPresion = 0;
    private static final long TIEMPO_MAXIMO = 1000; // 1 segundo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panico);

        btnPanico = findViewById(R.id.btnPanico);

        // Verifica permisos de ubicación y SMS
        verificarPermisos();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Al presionar el botón de pánico, se obtiene la ubicación y se envían los mensajes
        btnPanico.setOnClickListener(v -> obtenerUbicacionYEnviarMensajes());
    }

    private void verificarPermisos() {
        // Verifica si se tiene permiso para acceder a la ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }

        // Verifica si se tiene permiso para enviar SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 101);
        }
    }

    // Método para obtener la ubicación y luego enviar el mensaje
    private void obtenerUbicacionYEnviarMensajes() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitud = location.getLatitude();
                longitud = location.getLongitude();

                // Obtener la dirección de la ubicación
                obtenerDireccionYEnviarSMS(latitud, longitud);
            } else {
                Toast.makeText(getApplicationContext(), "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para obtener la dirección a partir de las coordenadas y enviar los mensajes
    private void obtenerDireccionYEnviarSMS(double latitud, double longitud) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitud, longitud, 1);

            String direccion = "";
            if (!addresses.isEmpty()) {
                direccion = addresses.get(0).getAddressLine(0);
            } else {
                direccion = "Lat: " + latitud + ", Lng: " + longitud;
            }

            enviarMensajes(direccion);
        } catch (IOException e) {
            e.printStackTrace();
            enviarMensajes("Lat: " + latitud + ", Lng: " + longitud);
        }
    }

    // Método para enviar los mensajes con la ubicación y el mensaje de emergencia
    private void enviarMensajes(String direccion) {
        SosMujerSqlite db = new SosMujerSqlite(this);
        int usuarioId = db.getUsuarioId();

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(URL_CONTACTOS + usuarioId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    if (response.length() == 0) {
                        Toast.makeText(getApplicationContext(), "No hay contactos registrados", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Preparar el mensaje de emergencia
                    String mensaje = "⚠️ ¡Emergencia! Necesito ayuda. " +
                            "Por favor comunícate conmigo lo antes posible. " +
                            "Ubicación: " + direccion + " - SOS Mujer";

                    // Enviar SMS a todos los contactos registrados
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject contacto = response.getJSONObject(i);
                        String numero = contacto.getString("numero");

                        // Agregar el código de país si no está
                        if (numero.length() == 9 && !numero.startsWith("+51")) {
                            numero = "+51" + numero;
                        }

                        try {
                            SmsManager sms = SmsManager.getDefault();
                            sms.sendTextMessage(numero, null, mensaje, null, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error al enviar SMS a " + numero, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error al preparar mensajes", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Toast.makeText(getApplicationContext(), "Error al cargar contactos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para capturar la triple pulsación de "Volumen Abajo"
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            long tiempoActual = System.currentTimeMillis();
            if (tiempoActual - tiempoUltimaPresion < TIEMPO_MAXIMO) {
                contadorPresiones++; // Incrementar el contador de presiones
            } else {
                // Reiniciar el contador si el tiempo entre presiones es mayor al intervalo
                contadorPresiones = 1;
            }

            // Actualizar la última vez que se presionó la tecla
            tiempoUltimaPresion = tiempoActual;

            // Si se han presionado 3 veces, ejecutar la acción de pánico
            if (contadorPresiones >= MAX_PRESIONES) {
                obtenerUbicacionYEnviarMensajes();
                contadorPresiones = 0;  // Reiniciar el contador después de la acción
                return true; // Evitar que se propague el evento
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
