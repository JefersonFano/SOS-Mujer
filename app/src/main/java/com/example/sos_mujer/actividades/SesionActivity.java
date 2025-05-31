package com.example.sos_mujer.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sos_mujer.R;
import com.example.sos_mujer.clases.Hash;
import com.example.sos_mujer.sqlite.SosMujerSqlite;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SesionActivity extends AppCompatActivity implements View.OnClickListener {
    EditText txtCorreo, txtContraseña;
    CheckBox chkRecordar;
    Button btnIniciar, btnCancelar;
    TextView lblPregRegistro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sesion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtCorreo = findViewById(R.id.sesTxtCorreo);
        txtContraseña = findViewById(R.id.sesTxtContraseña);
        chkRecordar = findViewById(R.id.sesChkRecordar);
        btnIniciar = findViewById(R.id.sesBtnIniciarSesion);
        btnCancelar = findViewById(R.id.sesBtnCancelar);
        lblPregRegistro = findViewById(R.id.sesLblPregRegistro);

        btnIniciar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        lblPregRegistro.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sesLblPregRegistro)
            registrar();
        else if (v.getId() == R.id.sesBtnIniciarSesion)
            iniciarSesion(txtCorreo.getText().toString(), txtContraseña.getText().toString());
        else if (v.getId() == R.id.sesBtnCancelar)
            cancelar();
    }

    private void iniciarSesion(String correo, String contrasenia) {
        SosMujerSqlite sosMujerSqlite = new SosMujerSqlite(this);
        Hash hash = new Hash();
        contrasenia = hash.StringToHash(contrasenia, "SHA256").toLowerCase();

        if(chkRecordar.isChecked()) {
            try {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                Date fechaNac = format.parse("1999-10-21");
                sosMujerSqlite.agregarUsuario(1, "Daniela", "Gonzales", correo,
                        contrasenia, "78654123", "987654321", "Av. Templo del sol 543", fechaNac);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        Intent iBienvenida = new Intent(this, BienvenidaActivity.class);
        iBienvenida.putExtra("usuario", "Rosa");
        startActivity(iBienvenida);
        finish();
    }

    private void registrar() {
        Intent iRegistrar = new Intent(this, RegistroActivity.class);
        startActivity(iRegistrar);
    }

    private void cancelar() {
        System.exit(1);
    }
}