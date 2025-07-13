package com.example.sos_mujer.fragmentos;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sos_mujer.R;
import com.example.sos_mujer.clases.ReporteMapa;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class MapaFragment extends Fragment {

    private GoogleMap mMap;

    private final OnMapReadyCallback callback = googleMap -> {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
        } else {
            mMap.setMyLocationEnabled(true);
        }

        cargarReportes();
    };

    private void cargarReportes() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://sos-mujer.atwebpages.com/ws/mostrarReporteMapa.php", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    ArrayList<ReporteMapa> reportes = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject obj = response.getJSONObject(i);
                        ReporteMapa rep = new ReporteMapa();
                        rep.id = obj.getInt("id");
                        rep.tipo = obj.getString("tipo");
                        rep.fecha = obj.getString("fecha");
                        rep.latitud = obj.getDouble("latitud");
                        rep.longitud = obj.getDouble("longitud");
                        rep.lugar = obj.getString("lugar");
                        reportes.add(rep);
                    }
                    mostrarAgrupados(reportes);
                } catch (Exception e) {
                    Log.e("Mapa", "Error parseando reportes: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("Mapa", "Error cargando reportes");
            }
        });
    }

    private void mostrarAgrupados(ArrayList<ReporteMapa> reportes) {
        HashMap<String, Integer> zonas = new HashMap<>();
        HashMap<String, LatLng> coordenadas = new HashMap<>();

        long ahora = System.currentTimeMillis();

        for (ReporteMapa rep : reportes) {
            String key = agruparPorZona(rep.latitud, rep.longitud);
            zonas.put(key, zonas.getOrDefault(key, 0) + 1);
            coordenadas.put(key, new LatLng(rep.latitud, rep.longitud));
        }

        for (String key : zonas.keySet()) {
            int cantidad = zonas.get(key);
            LatLng ubicacion = coordenadas.get(key);

            if (esReporteReciente(reportes, ubicacion, ahora)) {
                mMap.addMarker(new MarkerOptions()
                        .position(ubicacion)
                        .title("Reporte reciente")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.alertasosmujer)));
            } else {
                mMap.addMarker(new MarkerOptions()
                        .position(ubicacion)
                        .title("Zona: " + cantidad + " reportes")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.advertenciasosmujer)));
            }
        }

        if (!coordenadas.isEmpty()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new ArrayList<>(coordenadas.values()).get(0), 15));
        }
    }

    private String agruparPorZona(double lat, double lng) {
        // Agrupa zonas redondeando a 3 decimales (aprox 150m)
        double zonaLat = Math.round(lat * 1000.0) / 1000.0;
        double zonaLng = Math.round(lng * 1000.0) / 1000.0;
        return zonaLat + "," + zonaLng;
    }

    private boolean esReporteReciente(ArrayList<ReporteMapa> reportes, LatLng ubicacion, long ahora) {
        for (ReporteMapa rep : reportes) {
            LatLng loc = new LatLng(rep.latitud, rep.longitud);
            if (distancia(loc, ubicacion) < 0.15) { // Menos de 150 metros
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date fechaReporte = sdf.parse(rep.fecha);
                    long diffMin = (ahora - fechaReporte.getTime()) / 60000;
                    if (diffMin <= 5) return true;
                } catch (Exception ignored) {}
            }
        }
        return false;
    }

    private double distancia(LatLng a, LatLng b) {
        double R = 6371; // km
        double dLat = Math.toRadians(b.latitude - a.latitude);
        double dLon = Math.toRadians(b.longitude - a.longitude);
        double lat1 = Math.toRadians(a.latitude);
        double lat2 = Math.toRadians(b.latitude);

        double aVal = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(aVal), Math.sqrt(1 - aVal));
        return R * c;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mapa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}
