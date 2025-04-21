package com.example.sos_mujer.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sos_mujer.R;

public class CargaActivity extends AppCompatActivity implements View.OnClickListener {

    ProgressBar barCarga;
    Button btnIniciar, btnRegistrar;
    TextView txtCargar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carga);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        barCarga = findViewById(R.id.carBarCarga);
        btnIniciar = findViewById(R.id.actBtnIniciarSesion);
        btnRegistrar = findViewById(R.id.actBtnRegistrarse);
        txtCargar = findViewById(R.id.txtCargar);

        btnIniciar.setOnClickListener(this);
        btnRegistrar.setOnClickListener(this);

        Thread tCarga = new Thread(() -> {
            for (int i = 0; i <= barCarga.getMax(); i++) {
                final int progress = i;
                runOnUiThread(() -> barCarga.setProgress(progress));
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Después de la carga, ocultar barra y mostrar botones
            runOnUiThread(() -> {
                barCarga.setVisibility(View.GONE);
                btnIniciar.setVisibility(View.VISIBLE);
                btnRegistrar.setVisibility(View.VISIBLE);
                txtCargar.setVisibility(View.GONE);
            });
        });
        tCarga.start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.actBtnIniciarSesion)
            iniciarSesion();
        else if (v.getId() == R.id.actBtnRegistrarse)
            registrar();
    }

    private void iniciarSesion() {
        Intent iIniciar = new Intent(this, SesionActivity.class);
        startActivity(iIniciar);
    }

    private void registrar() {
        Intent iRegistro = new Intent(this, RegistroActivity.class);
        startActivity(iRegistro);
    }
}