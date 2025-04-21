package com.example.sos_mujer.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sos_mujer.R;

public class RegistroActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnRegistrar, btnCancelar;
    TextView lblPregInicio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnRegistrar = findViewById(R.id.regBtnRegistrar);
        btnCancelar = findViewById(R.id.regBtnCancelar);
        lblPregInicio = findViewById(R.id.regLblPregInicio);

        btnRegistrar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        lblPregInicio.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.regLblPregInicio)
            iniciarSesion();
        else if (v.getId() == R.id.regBtnRegistrar)
            registrar();
        else if (v.getId() == R.id.regBtnCancelar)
            cancelar();
    }

    private void iniciarSesion() {
        Intent iSesion = new Intent(this, SesionActivity.class);
        startActivity(iSesion);
    }

    private void registrar() {
    }

    private void cancelar() {
        System.exit(1);
    }
}