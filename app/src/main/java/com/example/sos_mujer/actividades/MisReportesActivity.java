package com.example.sos_mujer.actividades;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sos_mujer.R;
import com.example.sos_mujer.adaptadores.ReporteAdapter;
import com.example.sos_mujer.clases.Reporte;
import com.example.sos_mujer.sqlite.SosMujerSqlite;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MisReportesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReporteAdapter adapter;
    private ArrayList<Reporte> lista;
    private final static String URL_MIS_REPORTES = "http://sos-mujer.atwebpages.com/ws/mostrarMisReportes.php?usuario_id=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_reportes);

        recyclerView = findViewById(R.id.misRepRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        lista = new ArrayList<>();
        adapter = new ReporteAdapter(lista);
        recyclerView.setAdapter(adapter);

        SosMujerSqlite db = new SosMujerSqlite(this);
        int usuarioId = db.getUsuarioId();

        if (usuarioId != -1) {
            cargarMisReportes(usuarioId);
        } else {
            Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarMisReportes(int usuarioId) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(URL_MIS_REPORTES + usuarioId, null, new BaseJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                try {
                    JSONArray jsonArray = new JSONArray(rawJsonResponse);
                    lista.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                        Date fecha = format.parse(jsonArray.getJSONObject(i).getString("fecha"));

                        lista.add(new Reporte(
                                jsonArray.getJSONObject(i).getInt("id"),
                                jsonArray.getJSONObject(i).getString("foto"),
                                jsonArray.getJSONObject(i).getString("tipo"),
                                fecha,
                                jsonArray.getJSONObject(i).getString("lugar"),
                                jsonArray.getJSONObject(i).getString("descripcion")
                        ));
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException | java.text.ParseException e) {
                    Toast.makeText(getApplicationContext(), "Error al procesar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                Toast.makeText(getApplicationContext(), "Error al cargar reportes", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Object parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }
}
