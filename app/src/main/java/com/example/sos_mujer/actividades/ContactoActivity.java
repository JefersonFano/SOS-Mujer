package com.example.sos_mujer.actividades;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sos_mujer.R;
import com.example.sos_mujer.adaptadores.ContactoAdapter;
import com.example.sos_mujer.clases.Contacto;
import com.example.sos_mujer.sqlite.SosMujerSqlite;
import com.example.sos_mujer.utils.LanguageHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ContactoActivity extends AppCompatActivity {

    RecyclerView rv;
    Button btnRegistrar, btnCancelar;
    TextView lblContador;
    ArrayList<Contacto> lista = new ArrayList<>();
    ContactoAdapter adapter;

    private static final String URL_MOSTRAR = "http://sos-mujer.atwebpages.com/ws/mostrarContacto.php?usuario_id=";
    private static final String URL_ELIMINAR = "http://sos-mujer.atwebpages.com/ws/eliminarContacto.php";
    private static final String URL_EDITAR = "http://sos-mujer.atwebpages.com/ws/editarContacto.php";


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.sos_mujer.utils.FontScaleHelper.applyFontScale(this);
        setContentView(R.layout.activity_contacto);

        rv = findViewById(R.id.rvContactos);
        btnRegistrar = findViewById(R.id.contBtnRegistrar);
        btnCancelar = findViewById(R.id.contBtnCancelar);
        lblContador = findViewById(R.id.lblContador);

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ContactoAdapter(lista, new ContactoAdapter.ContactoListener() {
            @Override
            public void editar(Contacto c) {
                abrirEditor(c);
            }

            @Override
            public void eliminar(Contacto c) {
                eliminarContacto(c.id);
            }
        });

        rv.setAdapter(adapter);

        btnRegistrar.setOnClickListener(v -> {
            startActivity(new Intent(this, ContactoRegistroActivity.class));
        });

        btnCancelar.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarContactos();
    }

    private void cargarContactos() {
        SosMujerSqlite db = new SosMujerSqlite(this);
        int usuarioId = db.getUsuarioId();

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(URL_MOSTRAR + usuarioId, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                lista.clear();

                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject o = response.getJSONObject(i);

                        Contacto c = new Contacto();
                        c.id = o.getInt("id");
                        c.nombre = o.getString("nombre");
                        c.numero = o.getString("numero");

                        lista.add(c);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                adapter.notifyDataSetChanged();
                actualizarContador();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Toast.makeText(getApplicationContext(), "Error al cargar contactos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarContador() {
        lblContador.setText(lista.size() + "/3 contactos");
        btnRegistrar.setEnabled(lista.size() < 3);
    }

    private void eliminarContacto(int id) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("id", id);

        client.post(URL_ELIMINAR, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int status = response.getInt("status");
                    String mensaje = response.getString("mensaje");
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();

                    if (status == 1) {
                        cargarContactos();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void abrirEditor(Contacto c) {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottomsheet_editar_contacto);

        EditText txtNombre = dialog.findViewById(R.id.editarNombre);
        EditText txtNumero = dialog.findViewById(R.id.editarNumero);
        Button btnGuardar = dialog.findViewById(R.id.btnGuardarCambios);

        if (txtNombre != null) txtNombre.setText(c.nombre);
        if (txtNumero != null) txtNumero.setText(c.numero);

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> {
                String nuevoNombre = txtNombre.getText().toString().trim();
                String nuevoNumero = txtNumero.getText().toString().trim();

                if (nuevoNombre.isEmpty() || nuevoNumero.isEmpty()) {
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                // VALIDAR 9 DÍGITOS
                if (nuevoNumero.length() != 9) {
                    Toast.makeText(this, "El número debe tener exactamente 9 dígitos", Toast.LENGTH_SHORT).show();
                    return;
                }

                actualizarContacto(c.id, nuevoNombre, nuevoNumero);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void actualizarContacto(int id, String nombre, String numero) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("nombre", nombre);
        params.put("numero", numero);

        client.post(URL_EDITAR, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int status = response.getInt("status");
                    String mensaje = response.getString("mensaje");
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();

                    if (status == 1) {
                        cargarContactos();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
