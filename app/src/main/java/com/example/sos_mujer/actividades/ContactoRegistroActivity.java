package com.example.sos_mujer.actividades;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sos_mujer.R;
import com.example.sos_mujer.sqlite.SosMujerSqlite;
import com.example.sos_mujer.utils.LanguageHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ContactoRegistroActivity extends AppCompatActivity {

    EditText txtNombre, txtNumero;
    Button btnRegistrar, btnCancelar;
    private final static String URL_REGISTRAR = "http://sos-mujer.atwebpages.com/ws/agregarContacto.php";


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.sos_mujer.utils.FontScaleHelper.applyFontScale(this);
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

        if (numero.length() != 9) {
            Toast.makeText(this, "El número debe tener exactamente 9 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        SosMujerSqlite db = new SosMujerSqlite(this);
        int usuarioId = db.getUsuarioId();

        RequestParams params = new RequestParams();
        params.put("usuario_id", usuarioId);
        params.put("nombre", nombre);
        params.put("numero", numero);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(URL_REGISTRAR, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int status = response.getInt("status");
                    String mensaje = response.getString("mensaje");
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();

                    if (status == 1) {
                        finish();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
