package com.example.sos_mujer.actividades;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import cz.msebera.android.httpclient.Header;
import com.example.sos_mujer.R;

import com.example.sos_mujer.sqlite.SosMujerSqlite;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportarActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_PHOTO = 1;
    EditText txtDescripcion, txtDireccion;
    Spinner cboAbusos;
    ImageButton btnTomarFoto;
    ImageView imgVistaPrevia;
    Button btnReportar, btnCancelar;
    String sRutaTemporal;
    Uri uPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reportar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnTomarFoto = findViewById(R.id.repBtnTomarFoto);
        imgVistaPrevia = findViewById(R.id.imgVistaPrevia);
        btnReportar = findViewById(R.id.repBtnReportar);
        btnCancelar = findViewById(R.id.repBtnCancelar);
        btnCancelar = findViewById(R.id.repBtnCancelar);
        txtDescripcion = findViewById(R.id.repTxtDescripcion);
        txtDireccion = findViewById(R.id.repTxtDireccion);
        cboAbusos = findViewById(R.id.repCboAbuso);

        btnTomarFoto.setOnClickListener(this);
        btnReportar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);

        validarPermisos();
    }

    private void validarPermisos() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.repBtnTomarFoto)
            tomarFoto();
        else if(v.getId() == R.id.repBtnReportar)
            reportar();
        else if(v.getId() == R.id.repBtnCancelar)
            cancelar();
    }

    private void tomarFoto() {
        try {
            Intent iTomarFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            uPhoto = FileProvider.getUriForFile(this, "com.example.sos_mujer.provider", createImage());
            iTomarFoto.putExtra(MediaStore.EXTRA_OUTPUT, uPhoto);
            startActivityForResult(iTomarFoto, REQUEST_PHOTO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File createImage() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "JPG_"+timeStamp;
        File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(fileName, ".jpg", directorio);
        sRutaTemporal = imagen.getAbsolutePath();
        return imagen;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PHOTO){
            if(resultCode == RESULT_OK){
                imgVistaPrevia.setImageURI(uPhoto);
                Toast.makeText(this, "Acepto", Toast.LENGTH_SHORT).show();
            }
            else {
                File tmp = new File(sRutaTemporal);
                tmp.delete();
                Toast.makeText(this, "Cancelo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String convertirImagenABase64(String path) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            byte[] byteArray = stream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void reportar() {
        String descripcion = txtDescripcion.getText().toString();
        String direccion = txtDireccion.getText().toString();
        String tipo = cboAbusos.getSelectedItem().toString();
        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());
        String base64Foto = convertirImagenABase64(sRutaTemporal);

        SosMujerSqlite db = new SosMujerSqlite(this);
        int usuario_id = db.getUsuarioId();

        RequestParams params = new RequestParams();
        params.put("usuario_id", usuario_id);
        params.put("foto", base64Foto);
        params.put("tipo", tipo);
        params.put("fecha", fecha);
        params.put("lugar", direccion);
        params.put("descripcion", descripcion);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post("http://sos-mujer.atwebpages.com/ws/agregarReporte.php", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(getApplicationContext(), "Reporte enviado correctamente", Toast.LENGTH_SHORT).show();
                finish(); // vuelve a la actividad anterior
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), "Error al enviar el reporte", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelar() {
        finishAffinity();
    }
}