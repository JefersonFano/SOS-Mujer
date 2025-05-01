package com.example.sos_mujer.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sos_mujer.R;
import com.example.sos_mujer.clases.Menu;

public class BienvenidaActivity extends AppCompatActivity implements Menu {

    TextView lblSaludo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bienvenida);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lblSaludo = findViewById(R.id.bieLblSaludo);
        String usuario = getIntent().getStringExtra("usuario");
        lblSaludo.setText("Hola " + usuario);
    }

    @Override
    public void onClickMenu(int id) {
        Intent iMenu = new Intent(this, MenuActivity.class);
        iMenu.putExtra("id",id);
        startActivity(iMenu);
        finish();
    }
}