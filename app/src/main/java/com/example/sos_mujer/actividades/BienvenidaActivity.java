package com.example.sos_mujer.actividades;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sos_mujer.R;
import com.example.sos_mujer.clases.Menu;
import com.example.sos_mujer.sqlite.SosMujerSqlite;
import com.example.sos_mujer.utils.LanguageHelper;

public class BienvenidaActivity extends AppCompatActivity implements View.OnClickListener, Menu {

    TextView lblSaludo, lblContacto, lblReportar, lblPanico, lblPerfil;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.sos_mujer.utils.FontScaleHelper.applyFontScale(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bienvenida);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        lblContacto = findViewById(R.id.lblContacto);
        lblReportar = findViewById(R.id.lblReportar);
        lblPanico = findViewById(R.id.lblPanico);
        lblPerfil = findViewById(R.id.lblPerfil);

        lblSaludo = findViewById(R.id.bieLblSaludo);
        String nombre = getIntent().getStringExtra("nombre");
        String apellido = getIntent().getStringExtra("apellido");

        if (nombre == null || nombre.isEmpty() || apellido == null || apellido.isEmpty()) {
            SosMujerSqlite db = new SosMujerSqlite(this);
            nombre = db.getString("nombre");
            apellido = db.getString("apellido");
        }

        String nombreCompleto = "";
        if (nombre != null && !nombre.isEmpty() && apellido != null && !apellido.isEmpty()) {
            nombreCompleto = nombre + " " + apellido.charAt(0) + ".";
        }

        lblSaludo.setText("Hola " + nombreCompleto);

        lblReportar.setOnClickListener(this);
        lblContacto.setOnClickListener(this);
        lblReportar.setOnClickListener(this);
        lblPanico.setOnClickListener(this);
        lblPerfil.setOnClickListener(this);
    }

    @Override
    public void onClickMenu(int id) {
        Intent iMenu = new Intent(this, MenuActivity.class);
        iMenu.putExtra("id",id);
        startActivity(iMenu);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.lblContacto)
            contacto();
        else if (v.getId() == R.id.lblReportar)
            reportar();
        else if (v.getId() == R.id.lblPanico)
            panico();
        else if (v.getId() == R.id.lblPerfil)
            perfil();
    }

    private void contacto() {
        Intent iContacto = new Intent(this, ContactoActivity.class);
        startActivity(iContacto);
    }

    private void reportar() {
        Intent iReportar = new Intent(this, ReportarActivity.class);
        startActivity(iReportar);
    }

    private void panico() {
        Intent iPanico = new Intent(this, PanicoActivity.class);
        startActivity(iPanico);
    }

    private void perfil() {
        Intent iPerfil = new Intent(this, PerfilActivity.class);
        startActivity(iPerfil);
    }
}