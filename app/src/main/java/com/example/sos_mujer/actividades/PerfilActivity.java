package com.example.sos_mujer.actividades;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sos_mujer.R;

public class PerfilActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnGuardar, btnCancelar, btnCerrarSesion;

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
        btnGuardar = findViewById(R.id.perfilBtnGuardar);
        btnCancelar = findViewById(R.id.perfilBtnCancelar);
        btnCerrarSesion = findViewById(R.id.perfilBtnCerrarSesion);

        btnGuardar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        btnCerrarSesion.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.perfilBtnGuardar)
            guardar();
        else if (v.getId() == R.id.perfilBtnCancelar)
            cancelar();
        else if (v.getId() == R.id.perfilBtnCerrarSesion)
            cerrarSesion();
    }

    private void guardar() {
    }

    private void cancelar() {
        System.exit(1);
    }

    private void cerrarSesion() {
    }
}