package com.example.sos_mujer.actividades;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sos_mujer.R;
import com.example.sos_mujer.sqlite.SosMujerSqlite;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

public class ContactoRegistroActivity extends AppCompatActivity {

    EditText txtNombre, txtNumero;
    Button btnRegistrar, btnCancelar;
    private final static String URL_REGISTRAR = "http://sos-mujer.atwebpages.com/ws/agregarContacto.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacto_registro);

        txtNombre = findViewById(R.id.NombreContacto);
        txtNumero = findViewById(R.id.TelefonoContacto);
        btnRegistrar = findViewById(R.id.agrContBtnRegistrar);
        btnCancelar = findViewById(R.id.agrContBtnCancelar);

        btnRegistrar.setOnClickListener(v -> registrar());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void registrar() {
        String nombre = txtNombre.getText().toString().trim();
        String numero = txtNumero.getText().toString().trim();

        if (nombre.isEmpty() || numero.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        SosMujerSqlite db = new SosMujerSqlite(this);
        int usuarioId = db.getUsuarioId();

        RequestParams params = new RequestParams();
        params.put("usuario_id", usuarioId);
        params.put("nombre", nombre);
        params.put("numero", numero);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(URL_REGISTRAR, params, new com.loopj.android.http.JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                try {
                    if (response.getInt("status") == 1) {
                        Toast.makeText(getApplicationContext(), "Contacto registrado", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), response.getString("mensaje"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
