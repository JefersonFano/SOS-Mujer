package com.example.sos_mujer.actividades;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SesionActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String urlIniciarSesion = "http://sos-mujer.atwebpages.com/ws/iniciarSesion.php";
    EditText txtCorreo, txtContraseña;
    CheckBox chkRecordar;
    Button btnIniciar, btnCancelar;
    TextView lblPregRegistro;

    private static final String PREFS_NAME = "SosMujerPrefs";
    private static final String KEY_MANTENER_SESION = "mantener_sesion";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si debemos mantener la sesión
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean mantenerSesion = prefs.getBoolean(KEY_MANTENER_SESION, false);

        SosMujerSqlite db = new SosMujerSqlite(this);

        if (!mantenerSesion) {
            // Si no se debe mantener la sesión, limpiamos la base de datos local al iniciar
            // Solo si venimos de un reinicio de app, no si estamos navegando
            // Pero como esta Activity es el launcher, está bien limpiar aquí si no hay sesión persistente.
            db.eliminarUsuario(db.getUsuarioId());
        }

        if (db.recordarSesion()) {
            String nombre = db.getString("nombre");
            String apellido = db.getString("apellido");
            Intent iBienvenida = new Intent(this, BienvenidaActivity.class);
            iBienvenida.putExtra("nombre", nombre);
            iBienvenida.putExtra("apellido", apellido);
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

                        // Guardar en SharedPreferences si se desea mantener la sesión
                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(KEY_MANTENER_SESION, chkRecordar.isChecked());
                        editor.apply();

                        // SIEMPRE guardamos en SQLite para que la app funcione mientras esté abierta
                        SosMujerSqlite db = new SosMujerSqlite(getApplicationContext());

                        // Limpia sesiones anteriores antes de guardar
                        // Obtenemos ID existente si hay uno, o limpiamos todo por seguridad
                        int idExistente = db.getUsuarioId();
                        if (idExistente != -1) db.eliminarUsuario(idExistente);
                        // O un borrado genérico si tuviéramos un método deleteAll (pero eliminarUsuario con ID funciona si solo hay 1)

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


                        Intent intent = new Intent(getApplicationContext(), BienvenidaActivity.class);
                        intent.putExtra("nombre", usuario.getString("nombre"));
                        intent.putExtra("apellido", usuario.getString("apellido"));
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
    }

    private void registrar() {
        Intent iRegistrar = new Intent(this, RegistroActivity.class);
        startActivity(iRegistrar);
    }

    private void cancelar() {
        finishAffinity();
    }
}