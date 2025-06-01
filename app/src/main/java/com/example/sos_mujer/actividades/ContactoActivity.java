package com.example.sos_mujer.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sos_mujer.R;
import com.example.sos_mujer.sqlite.SosMujerSqlite;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ContactoActivity extends AppCompatActivity {

    Button btnRegistrar, btnCancelar;
    ListView listaContactos;
    ArrayList<String> datos = new ArrayList<>();
    ArrayAdapter<String> adapter;

    private final static String URL_CONTACTOS = "http://sos-mujer.atwebpages.com/ws/mostrarContacto.php?usuario_id=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacto);

        btnRegistrar = findViewById(R.id.contBtnRegistrar);
        btnCancelar = findViewById(R.id.contBtnCancelar);
        listaContactos = findViewById(R.id.listaContactos);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, datos);
        listaContactos.setAdapter(adapter);

        btnRegistrar.setOnClickListener(v -> registrar());
        btnCancelar.setOnClickListener(v -> finish());

        cargarContactos();
    }

    private void cargarContactos() {
        SosMujerSqlite db = new SosMujerSqlite(this);
        int usuarioId = db.getUsuarioId();

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(URL_CONTACTOS + usuarioId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                datos.clear();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject contacto = response.getJSONObject(i);
                        String linea = contacto.getString("nombre") + " - " + contacto.getString("numero");
                        datos.add(linea);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                adapter.notifyDataSetChanged();

                // Si hay 3 o más contactos, desactiva el botón
                if (datos.size() >= 3) {
                    btnRegistrar.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "Ya tienes 3 contactos registrados", Toast.LENGTH_SHORT).show();
                } else {
                    btnRegistrar.setEnabled(true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Toast.makeText(getApplicationContext(), "Error al cargar contactos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registrar() {
        Intent i = new Intent(this, ContactoRegistroActivity.class);
        startActivity(i);
    }
}
