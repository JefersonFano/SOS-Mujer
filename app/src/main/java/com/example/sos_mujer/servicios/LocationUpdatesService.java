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
import com.example.sos_mujer.utils.EmergenciaManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class LocationUpdatesService extends Service {

    private static final String CHANNEL_ID = "sos_tracking";
    private static final int NOTIF_ID = 55;

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

        iniciarNotificacion();
        iniciarTracking();

        return START_STICKY;
    }

    private void iniciarNotificacion() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    "Seguimiento SOS Mujer",
                    NotificationManager.IMPORTANCE_LOW
            );
            nm.createNotificationChannel(canal);
        }

        Intent intent = new Intent(this, PanicoActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Emergencia activa")
                .setContentText("Enviando ubicación en vivo…")
                .setSmallIcon(R.drawable.ic_location)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();

        startForeground(NOTIF_ID, notif);
    }

    private void iniciarTracking() {

        LocationRequest req = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(3000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) {
                    enviarUbicacion(
                            usuarioId,
                            loc.getLatitude(),
                            loc.getLongitude()
                    );
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }

        fusedLocationClient.requestLocationUpdates(req, locationCallback, null);
    }

    private void enviarUbicacion(int usuarioId, double lat, double lon) {

        int emergenciaId = EmergenciaManager.getEmergenciaId(this);
        if (emergenciaId == -1) {
            Log.e("SOS", "No hay emergencia_id activo");
            return;
        }

        String url = "http://sos-mujer.atwebpages.com/ws/actualizar_ubicacion.php";

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams p = new RequestParams();

        p.put("usuario_id", usuarioId);
        p.put("latitud", lat);
        p.put("longitud", lon);
        p.put("emergencia_id", emergenciaId);

        client.post(url, p, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int s, Header[] h, org.json.JSONObject r) {
                Log.d("SOS", "Ubicación enviada");
            }

            @Override
            public void onFailure(int s, Header[] h, Throwable t, org.json.JSONObject e) {
                Log.e("SOS", "Error enviando ubicación");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationCallback != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
