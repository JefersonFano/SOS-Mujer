package com.example.sos_mujer.servicios;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.sos_mujer.R;
import com.example.sos_mujer.actividades.PanicoActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

public class LocationUpdatesService extends Service {

    private static final String CHANNEL_ID = "sos_mujer_tracking_channel";
    private static final int NOTIFICATION_ID = 12345;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private int usuarioId;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        usuarioId = intent.getIntExtra("usuario_id", -1);

        if (usuarioId == -1) {
            stopSelf();
            return START_NOT_STICKY;
        }

        iniciarNotificacionForeground();
        iniciarActualizacionUbicacion();

        return START_STICKY;
    }

    private void iniciarNotificacionForeground() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    "Ubicación en vivo - SOS Mujer",
                    NotificationManager.IMPORTANCE_LOW
            );
            manager.createNotificationChannel(canal);
        }

        Intent intent = new Intent(this, PanicoActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Enviando ubicación en vivo…")
                .setContentText("Tus contactos pueden ver tu ubicación en tiempo real")
                .setSmallIcon(R.drawable.ic_location)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notif);
    }

    private void iniciarActualizacionUbicacion() {

        LocationRequest request = LocationRequest.create()
                .setInterval(5000) // cada 5 segundos
                .setFastestInterval(3000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {

                Location location = result.getLastLocation();
                if (location != null) {
                    enviarUbicacionAlServidor(
                            usuarioId,
                            location.getLatitude(),
                            location.getLongitude()
                    );
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }

        fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                null
        );
    }

    private void enviarUbicacionAlServidor(int usuarioId, double lat, double lon) {

        String url = "http://sos-mujer.atwebpages.com/ws/actualizar_ubicacion.php";

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("usuario_id", usuarioId);
        params.put("latitud", lat);
        params.put("longitud", lon);

        client.post(url, params, new com.loopj.android.http.JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int s, cz.msebera.android.httpclient.Header[] h, org.json.JSONObject response) {
                Log.d("LiveLocation", "Ubicación guardada");
            }

            @Override
            public void onFailure(int s, cz.msebera.android.httpclient.Header[] h, Throwable t, org.json.JSONObject e) {
                Log.e("LiveLocation", "Error al enviar ubicación");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationCallback != null && fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
