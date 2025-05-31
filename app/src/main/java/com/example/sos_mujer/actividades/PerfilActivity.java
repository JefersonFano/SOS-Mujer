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

public class PerfilActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnGuardar, btnCancelar, btnCerrarSesion;
    EditText txtNombre, txtApellido, txtCorreo, txtTelefono, txtDireccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnGuardar = findViewById(R.id.perfilBtnActualizar);
        btnCancelar = findViewById(R.id.perfilBtnCancelar);
        btnCerrarSesion = findViewById(R.id.perfilBtnCerrarSesion);

        btnGuardar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        btnCerrarSesion.setOnClickListener(this);

        cargarConfiguracion();
    }

    private void cargarConfiguracion() {
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

    private void actualizar() {
    }

    private void cancelar() {
        System.exit(1);
    }

    private void cerrarSesion() {
        SosMujerSqlite sosMujerSqlite = new SosMujerSqlite(this); // en vez de getContext()q
        sosMujerSqlite.eliminarUsuario(1);
        Intent iSesion = new Intent(this, SesionActivity.class);
        startActivity(iSesion);
        finish();
    }
}