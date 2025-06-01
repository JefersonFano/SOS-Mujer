package com.example.sos_mujer.actividades;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sos_mujer.R;
import com.example.sos_mujer.clases.Hash;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

public class RegistroActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String urlAgregarUsuario = "http://sos-mujer.atwebpages.com/ws/agregarUsuario.php";
    EditText txtNombre, txtApellido, txtCorreo, txtContrasenia, txtConfirmarContrasenia, txtDireccion, txtFechaNac, txtDni, txtTelefono;
    Button btnRegistrar, btnCancelar;
    CheckBox chkTerminos;
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
        txtNombre = findViewById(R.id.regTxtNombre);
        txtApellido = findViewById(R.id.regTxtApellido);
        txtCorreo = findViewById(R.id.regTxtCorreo);
        txtContrasenia = findViewById(R.id.regTxtContrasenia);
        txtConfirmarContrasenia = findViewById(R.id.regTxtConfirmarContrasenia);
        txtDireccion = findViewById(R.id.regTxtDireccion);
        txtFechaNac = findViewById(R.id.regTxtFechaNacimiento);
        txtDni = findViewById(R.id.regTxtDni);
        txtTelefono = findViewById(R.id.regTxtTelefono);
        chkTerminos = findViewById(R.id.regChkTerminos);

        btnRegistrar = findViewById(R.id.regBtnRegistrar);
        btnCancelar = findViewById(R.id.regBtnCancelar);
        lblPregInicio = findViewById(R.id.regLblPregInicio);

        btnRegistrar.setOnClickListener(this);
        txtFechaNac.setOnClickListener(this);
        chkTerminos.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        lblPregInicio.setOnClickListener(this);

        btnRegistrar.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.regTxtFechaNacimiento)
            seleccionarFecha();
        else if (v.getId() == R.id.regChkTerminos)
            mostrarTerminos();
        else if (v.getId() == R.id.regLblPregInicio)
            iniciarSesion();
        else if (v.getId() == R.id.regBtnRegistrar)
            registrar();
        else if (v.getId() == R.id.regBtnCancelar)
            cancelar();
    }

    private void seleccionarFecha() {
        DatePickerDialog dpd;
        final Calendar fechaActual = Calendar.getInstance();
        int dia = fechaActual.get(Calendar.DAY_OF_MONTH);   //1..28/29/30/31
        int mes = fechaActual.get(Calendar.MONTH);          //0..11
        int anio = fechaActual.get(Calendar.YEAR);           //2025
        dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int y, int m, int d) {
                txtFechaNac.setText(y+"-"+((m+1)<10?"0"+(m+1):(m+1))+"-"+(d<10?"0"+d:d));
            }
        }, anio, mes, dia);
        dpd.show();
        dpd.getButton(DatePickerDialog.BUTTON_POSITIVE).setText("Aceptar");
        dpd.getButton(DatePickerDialog.BUTTON_POSITIVE).setAllCaps(false);
        dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE).setText("Cancelar");
        dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE).setAllCaps(false);

    }

    private void mostrarTerminos(){
        if (!chkTerminos.isChecked())
            return;
        AlertDialog.Builder terminos = new AlertDialog.Builder(this);
        terminos.setTitle("Terminos y condiciones");
        terminos.setMessage("1. Aceptación de los términos: El uso de la aplicación Lima Segura implica la aceptación plena y sin reservas de los presentes Términos y Condiciones. Si no estás de acuerdo con ellos, por favor no utilices la aplicación.\n" +
                "2. Descripción del servicio: Lima Segura es una aplicación móvil que proporciona información sobre seguridad ciudadana, alertas, recomendaciones y herramientas de reporte para residentes y visitantes de Lima, Perú. El objetivo es fomentar la prevención y la respuesta ante situaciones de emergencia.\n" +
                "3. Registro de usuarios: En algunos casos, el uso completo de la app puede requerir el registro de una cuenta personal. El usuario se compromete a proporcionar información veraz, actualizada y completa durante el proceso de registro. \n" +
                "4. Uso adecuado de la aplicación: El usuario se compromete a: \n" +
                "- No utilizar la app con fines ilícitos, fraudulentos o maliciosos.\n"+
                "- No utilizar la app con fines ilícitos, fraudulentos o maliciosos.\n" +
                "- No utilizar la app con fines ilícitos, fraudulentos o maliciosos.\n" +
                "5. Notificaciones y geolocalización: La app puede enviar notificaciones en tiempo real y utilizar datos de geolocalización para brindar información relevante sobre tu ubicación. Al aceptar estos términos, autorizas el uso de esta funcionalidad. Puedes desactivarla en cualquier momento desde la configuración de tu dispositivo." +
                "6. Responsabilidad: Lima Segura no se hace responsable por: \n" +
                "- No utilizar la app con fines ilícitos, fraudulentos o maliciosos.\n"+
                "- No utilizar la app con fines ilícitos, fraudulentos o maliciosos.\n" +
                "- No utilizar la app con fines ilícitos, fraudulentos o maliciosos.\n" +
                "7. Propiedad intelectual: Todos los contenidos, logos, diseños y funcionalidades de la app son propiedad de Lima Segura o de sus respectivos licenciantes, y están protegidos por las leyes de propiedad intelectual. Su reproducción sin autorización está prohibida." +
                "8. Política de privacidad: El uso de esta app implica la aceptación de nuestra [Política de Privacidad], donde se detalla cómo recopilamos, utilizamos y protegemos tu información personal.");
        chkTerminos.setChecked(false);
        terminos.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chkTerminos.setChecked(true);
                btnRegistrar.setEnabled(true);
                dialog.dismiss();
            }
        });
        terminos.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alerta = terminos.create();
        alerta.setCancelable(false);
        alerta.setCanceledOnTouchOutside(false);
        alerta.show();
        alerta.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        alerta.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);

    }

    private void iniciarSesion() {
        Intent iSesion = new Intent(this, SesionActivity.class);
        startActivity(iSesion);
    }

    private void registrar() {
        Hash hash = new Hash();

        if(!validarFormulario()){
            return;
        }
        AsyncHttpClient ahcRegistrar = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("nombre", txtNombre.getText().toString());
        params.put("apellido", txtApellido.getText().toString());
        params.put("correo", txtCorreo.getText().toString());
        params.put("contrasenia", hash.StringToHash(txtContrasenia.getText().toString(),"SHA256").toLowerCase());
        params.put("dni", txtDni.getText().toString());
        params.put("telefono", txtTelefono.getText().toString());
        params.put("direccion", txtDireccion.getText().toString());
        params.put("fechaNac", txtFechaNac.getText().toString());

        ahcRegistrar.post(urlAgregarUsuario, params, new BaseJsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                int status = rawJsonResponse.isEmpty() ? 0 : Integer.parseInt(rawJsonResponse);
                if (status == 1){
                    Toast.makeText(getApplicationContext(), "Usuario Agregado", Toast.LENGTH_SHORT).show();
                    Intent iSesion = new Intent(getApplicationContext(), SesionActivity.class);
                    startActivity(iSesion);
                    finish();
                }
                else
                    Toast.makeText(getApplicationContext(), "ERROR: al crear usuario", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                Toast.makeText(getApplicationContext(), "ERROR:" + statusCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Object parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    private boolean validarFormulario(){
        //ningun campo este vacio
        if (txtNombre.getText().toString().trim().isEmpty() ||
                txtApellido.getText().toString().trim().isEmpty() ||
                txtCorreo.getText().toString().trim().isEmpty() ||
                txtContrasenia.getText().toString().trim().isEmpty() ||
                txtConfirmarContrasenia.getText().toString().trim().isEmpty() ||
                txtDni.getText().toString().trim().isEmpty() ||
                txtTelefono.getText().toString().trim().isEmpty()||
                txtDireccion.getText().toString().trim().isEmpty()||
                txtFechaNac.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "debe completar todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        //regex para DNI, correo
        String dni = txtDni.getText().toString().trim();
        if (!dni.matches("\\d{8}")){
            Toast.makeText(this, "El dni debe tener 8 digitos", Toast.LENGTH_SHORT).show();
            return false;
        }
        String correo = txtCorreo.getText().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()){
            Toast.makeText(this, "El formato de correo Electronico no es valido", Toast.LENGTH_SHORT).show();
            return false;
        }
        //clave y confirmar clave sean iguales
        String clave1 = txtContrasenia.getText().toString().trim();
        String clave2 = txtConfirmarContrasenia.getText().toString().trim();
        if (!clave1.equals(clave2)){
            Toast.makeText(this, "Las claves deben ser iguales",Toast.LENGTH_SHORT).show();
            return false;
        }
        //haya aceptador los terminos
        if (!chkTerminos.isChecked()){
            Toast.makeText(this, "Debe aceptar los terminos y condiciones", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void cancelar() {
        System.exit(1);
    }
}