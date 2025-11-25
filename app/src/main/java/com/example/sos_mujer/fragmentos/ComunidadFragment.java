package com.example.sos_mujer.fragmentos;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sos_mujer.R;
import com.example.sos_mujer.adaptadores.ReporteAdapter;
import com.example.sos_mujer.clases.Reporte;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class ComunidadFragment extends Fragment {

    private final static String urlMostrarReportes =
            "http://sos-mujer.atwebpages.com/ws/mostrarReportes.php";

    private RecyclerView recReportes;
    private Spinner spinnerFiltro;
    private TextView txtTotal;
    private ProgressBar progressCarga;

    private ReporteAdapter adapter;
    public static ArrayList<Reporte> lista;

    private boolean mostrarCercanos = false;
    private double userLat = 0;
    private double userLon = 0;
    private static final double RADIO_KM = 3.0;

    FusedLocationProviderClient fusedLocation;

    public ComunidadFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_comunidad, container, false);

        recReportes = vista.findViewById(R.id.frgRepRecReportes);
        spinnerFiltro = vista.findViewById(R.id.spinnerFiltro);
        txtTotal = vista.findViewById(R.id.txtTotalReportes);
        progressCarga = vista.findViewById(R.id.progressCarga);

        lista = new ArrayList<>();
        adapter = new ReporteAdapter(lista);
        recReportes.setLayoutManager(new LinearLayoutManager(getContext()));
        recReportes.setAdapter(adapter);

        fusedLocation = LocationServices.getFusedLocationProviderClient(getActivity());
        obtenerUbicacionUsuario();

        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mostrarCercanos = (position == 1);
                progressCarga.setVisibility(View.VISIBLE);
                cargarReportes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return vista;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarReportes();
    }

    private void obtenerUbicacionUsuario() {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);

            return;
        }

        fusedLocation.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {

                userLat = location.getLatitude();
                userLon = location.getLongitude();

                ReporteAdapter.userLat = userLat;
                ReporteAdapter.userLon = userLon;
            }
        });
    }

    private void cargarReportes() {

        AsyncHttpClient cliente = new AsyncHttpClient();

        cliente.get(urlMostrarReportes, null, new BaseJsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {

                progressCarga.setVisibility(View.GONE);

                try {

                    JSONArray json = new JSONArray(rawJsonResponse);
                    lista.clear();

                    for (int i = 0; i < json.length(); i++) {

                        double lat = json.getJSONObject(i).getDouble("latitud");
                        double lon = json.getJSONObject(i).getDouble("longitud");

                        if (mostrarCercanos) {
                            double dist = calcularDistancia(userLat, userLon, lat, lon);
                            if (dist > RADIO_KM) continue;
                        }

                        Date fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                .parse(json.getJSONObject(i).getString("fecha"));

                        lista.add(new Reporte(
                                json.getJSONObject(i).getInt("id"),
                                json.getJSONObject(i).getString("foto"),
                                json.getJSONObject(i).getString("tipo"),
                                fecha,
                                json.getJSONObject(i).getString("lugar"),
                                json.getJSONObject(i).getString("descripcion"),
                                lat,
                                lon
                        ));
                    }

                    adapter.notifyDataSetChanged();
                    txtTotal.setText("Total: " + lista.size() + " reportes");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  String rawJsonData, Object errorResponse) {
                progressCarga.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error al cargar reportes", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Object parseResponse(String rawJsonData, boolean isFailure) {
                return null;
            }
        });
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {

        float[] res = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, res);
        return res[0] / 1000;
    }
}
