package com.example.sos_mujer.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import cz.msebera.android.httpclient.Header;
import com.example.sos_mujer.R;
import com.example.sos_mujer.clases.Hash;
import com.example.sos_mujer.sqlite.SosMujerSqlite;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SesionActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String urlIniciarSesion = "http://sos-mujer.atwebpages.com/ws/iniciarSesion.php";
    EditText txtCorreo, txtContraseña;
    CheckBox chkRecordar;
    Button btnIniciar, btnCancelar;
    TextView lblPregRegistro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SosMujerSqlite db = new SosMujerSqlite(this);
        if (db.recordarSesion()) {
            String nombre = db.getString("nombre");
            Intent iBienvenida = new Intent(this, BienvenidaActivity.class);
            iBienvenida.putExtra("usuario", nombre);
            startActivity(iBienvenida);
            finish();
            return; // salir del método para no cargar el layout
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sesion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtCorreo = findViewById(R.id.sesTxtCorreo);
        txtContraseña = findViewById(R.id.sesTxtContraseña);
        chkRecordar = findViewById(R.id.sesChkRecordar);
        btnIniciar = findViewById(R.id.sesBtnIniciarSesion);
        btnCancelar = findViewById(R.id.sesBtnCancelar);
        lblPregRegistro = findViewById(R.id.sesLblPregRegistro);

        btnIniciar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        lblPregRegistro.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sesLblPregRegistro)
            registrar();
        else if (v.getId() == R.id.sesBtnIniciarSesion)
            iniciarSesion(txtCorreo.getText().toString(), txtContraseña.getText().toString());
        else if (v.getId() == R.id.sesBtnCancelar)
            cancelar();
    }

    private void iniciarSesion(String correo, String contrasenia) {
        Hash hash = new Hash();
        String claveHasheada = hash.StringToHash(contrasenia, "SHA256").toLowerCase();

        RequestParams params = new RequestParams();
        params.put("correo", correo);
        params.put("contrasenia", claveHasheada);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(urlIniciarSesion, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int status = response.getInt("status");
                    if (status == 1) {
                        JSONObject usuario = response.getJSONObject("usuario");

                        if (chkRecordar.isChecked()) {
                            SosMujerSqlite db = new SosMujerSqlite(getApplicationContext());

                            // Limpia sesiones anteriores antes de guardar
                            db.eliminarUsuario(1);

                            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                            Date fechaNac = format.parse(usuario.getString("fechaNac"));

                            db.agregarUsuario(
                                    usuario.getInt("id"),
                                    usuario.getString("nombre"),
                                    usuario.getString("apellido"),
                                    usuario.getString("correo"),
                                    usuario.getString("contrasenia"),
                                    usuario.getString("dni"),
                                    usuario.getString("telefono"),
                                    usuario.getString("direccion"),
                                    fechaNac
                            );
                        }

                        Intent intent = new Intent(getApplicationContext(), BienvenidaActivity.class);
                        intent.putExtra("usuario", usuario.getString("nombre"));
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), response.getString("mensaje"), Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });

        /*SosMujerSqlite sosMujerSqlite = new SosMujerSqlite(this);
        Hash hash = new Hash();
        contrasenia = hash.StringToHash(contrasenia, "SHA256").toLowerCase();

        if(chkRecordar.isChecked()) {
            try {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                Date fechaNac = format.parse("1999-10-21");
                sosMujerSqlite.agregarUsuario(1, "Daniela", "Gonzales", correo,
                        contrasenia, "78654123", "987654321", "Av. Templo del sol 543", fechaNac);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        Intent iBienvenida = new Intent(this, BienvenidaActivity.class);
        iBienvenida.putExtra("usuario", "Rosa");
        startActivity(iBienvenida);
        finish();*/
    }

    private void registrar() {
        Intent iRegistrar = new Intent(this, RegistroActivity.class);
        startActivity(iRegistrar);
    }

    private void cancelar() {
        finishAffinity();
    }
}