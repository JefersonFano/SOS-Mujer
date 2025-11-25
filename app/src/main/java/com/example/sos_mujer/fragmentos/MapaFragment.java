package com.example.sos_mujer.fragmentos;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.sos_mujer.R;
import com.example.sos_mujer.clases.ReporteMapa;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class MapaFragment extends Fragment {

    private GoogleMap mMap;
    // Radio de agrupación en metros
    private final double RADIO_METROS = 200.0;

    // Para mostrar el mapa cuando esté listo
    private final OnMapReadyCallback callback = googleMap -> {
        mMap = googleMap;
        habilitarGPS();
        cargarReportes();

        // Click en marcador → abrir BottomSheet si es zona agrupada
        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof ClusterInfo) {
                mostrarVentanaEmergente((ClusterInfo) tag);
                return true; // consumimos el evento
            }
            return false; // comportamiento normal (muestra el snippet)
        });
    };

    private void habilitarGPS() {
        if (ActivityCompat.checkSelfPermission(
                getContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    10
            );
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void cargarReportes() {
        AsyncHttpClient client = new AsyncHttpClient();

        client.get("http://sos-mujer.atwebpages.com/ws/mostrarReporteMapa.php",
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        try {
                            ArrayList<ReporteMapa> lista = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);

                                ReporteMapa r = new ReporteMapa();
                                r.id = obj.getInt("id");
                                r.tipo = obj.getString("tipo");
                                r.fecha = obj.getString("fecha");
                                r.latitud = obj.getDouble("latitud");
                                r.longitud = obj.getDouble("longitud");
                                r.lugar = obj.getString("lugar");

                                lista.add(r);
                            }

                            agruparYMostrar(lista);

                        } catch (Exception e) {
                            Log.e("MAPA", "Error parseando: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.e("MAPA", "Error cargando reportes");
                    }
                });
    }

    // Agrupa por radio y dibuja en el mapa
    private void agruparYMostrar(ArrayList<ReporteMapa> lista) {

        long ahora = System.currentTimeMillis();
        boolean[] agrupado = new boolean[lista.size()];

        for (int i = 0; i < lista.size(); i++) {

            if (agrupado[i]) continue;

            ReporteMapa base = lista.get(i);
            LatLng posBase = new LatLng(base.latitud, base.longitud);

            ArrayList<ReporteMapa> grupo = new ArrayList<>();
            grupo.add(base);
            agrupado[i] = true;

            // Buscar otros reportes cercanos a "base"
            for (int j = i + 1; j < lista.size(); j++) {
                if (agrupado[j]) continue;

                ReporteMapa otro = lista.get(j);
                LatLng posOtro = new LatLng(otro.latitud, otro.longitud);

                double distKm = distancia(posBase, posOtro);

                if (distKm <= RADIO_METROS / 1000.0) {
                    grupo.add(otro);
                    agrupado[j] = true;
                }
            }

            dibujarGrupo(grupo, ahora);
        }

        if (!lista.isEmpty()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lista.get(0).latitud, lista.get(0).longitud),
                    15
            ));
        }
    }

    // Dibuja un grupo: o marcador individual o zona peligrosa
    private void dibujarGrupo(ArrayList<ReporteMapa> grupo, long ahora) {

        int total = grupo.size();
        int robo = 0, fisica = 0, sexual = 0, acoso = 0;
        boolean hayReciente = false;
        ReporteMapa reporteReciente = null; // <-- nuevo

        LatLng pos = new LatLng(grupo.get(0).latitud, grupo.get(0).longitud);

        try {
            for (ReporteMapa rep : grupo) {

                switch (rep.tipo) {
                    case "Robo": robo++; break;
                    case "Violencia física": fisica++; break;
                    case "Violencia sexual": sexual++; break;
                    case "Acoso": acoso++; break;
                }

                // ¿Reporte reciente?
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date fecha = sdf.parse(rep.fecha);

                long diffMin = (ahora - fecha.getTime()) / 60000;

                if (diffMin <= 5) {
                    hayReciente = true;
                    reporteReciente = rep; // guardamos el último reciente
                }
            }

        } catch (Exception ignored) {}

        // --- 1) MARCADOR INDIVIDUAL RECIENTE SI EXISTE ---
        if (hayReciente && reporteReciente != null) {

            int icono = obtenerIcono(reporteReciente.tipo);

            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(reporteReciente.latitud, reporteReciente.longitud))
                    .title("Reporte reciente: " + reporteReciente.tipo)
                    .snippet(reporteReciente.lugar)
                    .icon(BitmapDescriptorFactory.fromResource(icono)));
        }

        // --- 2) MOSTRAR GRUPO NORMALMENTE ---
        if (total == 1) {
            // Si es 1 reporte, ya se mostró arriba como reciente.
            return;
        }

        int iconoZona = hayReciente
                ? R.drawable.alertasosmujer   // si hay reciente, poner alerta
                : R.drawable.advertenciasosmujer;

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title("Zona peligrosa")
                .snippet("Toca para ver detalles")
                .icon(BitmapDescriptorFactory.fromResource(iconoZona)));

        ClusterInfo info = new ClusterInfo(total, robo, fisica, sexual, acoso);
        if (marker != null) marker.setTag(info);
    }

    // Devuelve icono según tipo
    private int obtenerIcono(String tipo) {
        switch (tipo) {
            case "Robo":
                return R.drawable.icono_robo;
            case "Violencia física":
                return R.drawable.icono_fisica;
            case "Violencia sexual":
                return R.drawable.icono_sexual;
            case "Acoso":
                return R.drawable.icono_acoso;
            default:
                return R.drawable.advertenciasosmujer;
        }
    }

    // Distancia en km usando fórmula de Haversine
    private double distancia(LatLng a, LatLng b) {
        double R = 6371; // km
        double dLat = Math.toRadians(b.latitude - a.latitude);
        double dLon = Math.toRadians(b.longitude - a.longitude);
        double lat1 = Math.toRadians(a.latitude);
        double lat2 = Math.toRadians(b.latitude);

        double val = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2)
                * Math.cos(lat1) * Math.cos(lat2);

        return 2 * R * Math.atan2(Math.sqrt(val), Math.sqrt(1 - val));
    }

    // ---------- BottomSheet moderno ----------

    private void mostrarVentanaEmergente(ClusterInfo info) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());

        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(16);
        root.setPadding(padding, padding, padding, padding);
        root.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));

        // Handler superior (barrita)
        View handler = new View(getContext());
        LinearLayout.LayoutParams handlerParams =
                new LinearLayout.LayoutParams(dp(80), dp(6));
        handlerParams.setMargins(0, 0, 0, dp(20));
        handlerParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        handler.setLayoutParams(handlerParams);
        handler.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.handler_background));
        handler.setAlpha(0.4f);
        root.addView(handler);

        // Título
        TextView titulo = new TextView(getContext());
        titulo.setText("Zona peligrosa");
        titulo.setTextSize(20);
        titulo.setTextColor(ContextCompat.getColor(getContext(), R.color.morado));
        titulo.setTypeface(titulo.getTypeface(), android.graphics.Typeface.BOLD);
        titulo.setPadding(0, 0, 0, dp(16));
        root.addView(titulo);

        // Tarjeta total
        LinearLayout cardTotal = crearTarjeta("Total de reportes",
                info.total + "", R.drawable.alertasosmujer);
        root.addView(cardTotal);

        // Tarjetas por tipo
        root.addView(crearTarjeta("Robo", String.valueOf(info.robo), R.drawable.icono_robo));
        root.addView(crearTarjeta("Violencia física", String.valueOf(info.fisica), R.drawable.icono_fisica));
        root.addView(crearTarjeta("Violencia sexual", String.valueOf(info.sexual), R.drawable.icono_sexual));
        root.addView(crearTarjeta("Acoso", String.valueOf(info.acoso), R.drawable.icono_acoso));

        dialog.setContentView(root);
        dialog.show();
    }

    private LinearLayout crearTarjeta(String titulo, String valor, int iconoResId) {

        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        int pad = dp(12);
        card.setPadding(pad, pad, pad, pad);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        params.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(params);

        card.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.card_background));

        // Icono
        ImageView img = new ImageView(getContext());
        img.setImageResource(iconoResId);
        LinearLayout.LayoutParams pImg =
                new LinearLayout.LayoutParams(dp(36), dp(36));
        pImg.setMargins(0, 0, dp(12), 0);
        img.setLayoutParams(pImg);

        // Layout de textos
        LinearLayout textos = new LinearLayout(getContext());
        textos.setOrientation(LinearLayout.VERTICAL);

        TextView tTitulo = new TextView(getContext());
        tTitulo.setText(titulo);
        tTitulo.setTextSize(14);
        tTitulo.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));

        TextView tValor = new TextView(getContext());
        tValor.setText(valor);
        tValor.setTextSize(16);
        tValor.setTextColor(ContextCompat.getColor(getContext(), R.color.morado));
        tValor.setTypeface(tValor.getTypeface(), android.graphics.Typeface.BOLD);

        textos.addView(tTitulo);
        textos.addView(tValor);

        card.addView(img);
        card.addView(textos);

        return card;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    // Info del clúster que se pasa al BottomSheet
    private static class ClusterInfo {
        int total, robo, fisica, sexual, acoso;

        public ClusterInfo(int total, int robo, int fisica, int sexual, int acoso) {
            this.total = total;
            this.robo = robo;
            this.fisica = fisica;
            this.sexual = sexual;
            this.acoso = acoso;
        }
    }

    // ---------------- Fragment lifecycle ----------------

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
