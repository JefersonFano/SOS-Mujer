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

public class CargaActivity extends AppCompatActivity {

    ProgressBar barCarga;
    Button btnIniciar, btnRegistrar;
    TextView txtCargar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carga);

        barCarga = findViewById(R.id.carBarCarga);
        btnIniciar = findViewById(R.id.btnIniciarSesion);
        btnRegistrar = findViewById(R.id.btnRegistrarse);
        txtCargar = findViewById(R.id.txtCargar);

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
}