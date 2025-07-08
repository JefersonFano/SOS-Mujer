package com.example.sos_mujer.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sos_mujer.R;
import com.example.sos_mujer.sqlite.SosMujerSqlite;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class PerfilActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnGuardar, btnCancelar, btnCerrarSesion;
    EditText txtNombre, txtApellido, txtCorreo, txtTelefono, txtDireccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        // Referencias
        txtNombre = findViewById(R.id.perfilTxtNombre);
        txtApellido = findViewById(R.id.perfilTxtApellido);
        txtCorreo = findViewById(R.id.perfilTxtCorreo);
        txtTelefono = findViewById(R.id.perfilTxtTelefono);
        txtDireccion = findViewById(R.id.perfilTxtDireccion);

        btnGuardar = findViewById(R.id.perfilBtnActualizar);
        btnCancelar = findViewById(R.id.perfilBtnCancelar);
        btnCerrarSesion = findViewById(R.id.perfilBtnCerrarSesion);

        btnGuardar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        btnCerrarSesion.setOnClickListener(this);


        cargarConfiguracion();
    }

    private void cargarConfiguracion() {
        SosMujerSqlite db = new SosMujerSqlite(this);
        txtNombre.setText(db.getString("nombre"));
        txtApellido.setText(db.getString("apellido"));
        txtCorreo.setText(db.getString("correo"));
        txtTelefono.setText(db.getString("telefono"));
        txtDireccion.setText(db.getString("direccion"));
    }

    private final static String URL_ACTUALIZAR = "http://sos-mujer.atwebpages.com/ws/actualizarUsuario.php";

    private void actualizar() {
        SosMujerSqlite db = new SosMujerSqlite(this);
        int usuarioId = db.getUsuarioId();

        String nombre = txtNombre.getText().toString();
        String apellido = txtApellido.getText().toString();
        String correo = txtCorreo.getText().toString();
        String telefono = txtTelefono.getText().toString();
        String direccion = txtDireccion.getText().toString();

        RequestParams params = new RequestParams();
        params.put("id", usuarioId);
        params.put("nombre", nombre);
        params.put("apellido", apellido);
        params.put("correo", correo);
        params.put("telefono", telefono);
        params.put("direccion", direccion);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(URL_ACTUALIZAR, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(getApplicationContext(), "Datos actualizados", Toast.LENGTH_SHORT).show();

                // También actualiza localmente en SQLite
                db.actualizarDato(usuarioId, "nombre", nombre);
                db.actualizarDato(usuarioId, "apellido", apellido);
                db.actualizarDato(usuarioId, "correo", correo);
                db.actualizarDato(usuarioId, "telefono", telefono);
                db.actualizarDato(usuarioId, "direccion", direccion);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.perfilBtnActualizar)
            actualizar();
        else if (v.getId() == R.id.perfilBtnCancelar)
            cancelar();
        else if (v.getId() == R.id.perfilBtnCerrarSesion)
            cerrarSesion();
    }

    private void cancelar() {
        finish();
    }

    private void verMisReportes() {
        Intent i = new Intent(this, MisReportesActivity.class);
        startActivity(i);
    }

    private void cerrarSesion() {
        SosMujerSqlite db = new SosMujerSqlite(this);
        int idUsuario = db.getUsuarioId();
        if (idUsuario != -1) {
            db.eliminarUsuario(idUsuario);
        }
        Intent iSesion = new Intent(this, SesionActivity.class);
        startActivity(iSesion);
        finish();

        /*SosMujerSqlite sosMujerSqlite = new SosMujerSqlite(this); // en vez de getContext()q
        sosMujerSqlite.eliminarUsuario(1);
        Intent iSesion = new Intent(this, SesionActivity.class);
        startActivity(iSesion);
        finish();*/
    }
}