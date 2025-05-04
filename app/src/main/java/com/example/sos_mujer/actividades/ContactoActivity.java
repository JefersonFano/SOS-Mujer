package com.example.sos_mujer.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sos_mujer.R;

public class ContactoActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnRegistrar, btnCancelar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contacto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnRegistrar = findViewById(R.id.contBtnRegistrar);
        btnCancelar = findViewById(R.id.contBtnCancelar);

        btnRegistrar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.contBtnRegistrar)
            registrar();
        else if (v.getId() == R.id.contBtnCancelar)
            cancelar();
    }

    private void registrar() {
        Intent iContactoRegistro = new Intent(this, ContactoRegistroActivity.class);
        startActivity(iContactoRegistro);
    }

    private void cancelar() {
        System.exit(1);
    }
}