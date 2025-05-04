package com.example.sos_mujer.actividades;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sos_mujer.R;

public class PanicoActivity extends AppCompatActivity {

    private Button btnSOS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_panico);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSOS = findViewById(R.id.btnEnviarSOS);

        btnSOS.setOnClickListener(v -> {
            btnSOS.setEnabled(false);
            btnSOS.setText("Enviando...");

            new Handler().postDelayed(() -> {
                btnSOS.setText("✅ SOS ENVIADO");
                Toast.makeText(this, "Alerta simulada enviada", Toast.LENGTH_SHORT).show();
                btnSOS.setEnabled(true);
            }, 3000);
        });
    }
}