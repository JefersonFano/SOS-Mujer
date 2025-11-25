package com.example.sos_mujer.actividades;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
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

import com.example.sos_mujer.R;
import com.example.sos_mujer.sqlite.SosMujerSqlite;
import com.example.sos_mujer.utils.LanguageHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.Base64;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class ReportarActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_PHOTO = 1;
    EditText txtDescripcion, txtDireccion;
    Spinner cboAbusos;
    ImageButton btnTomarFoto;
    ImageView imgVistaPrevia;
    Button btnReportar, btnCancelar, btnUbicacion;
    String sRutaTemporal;
    Uri uPhoto;

    double latitud = 0.0;
    double longitud = 0.0;
    FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.sos_mujer.utils.FontScaleHelper.applyFontScale(this);
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
        btnUbicacion = findViewById(R.id.repBtnUbicacionActual);
        txtDescripcion = findViewById(R.id.repTxtDescripcion);
        txtDireccion = findViewById(R.id.repTxtDireccion);
        cboAbusos = findViewById(R.id.repCboAbuso);

        btnTomarFoto.setOnClickListener(this);
        btnReportar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        btnUbicacion.setOnClickListener(v -> obtenerUbicacion());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        validarPermisos();
    }

    private void validarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }
    }

    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitud = location.getLatitude();
                longitud = location.getLongitude();

                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(latitud, longitud, 1);
                    if (!addresses.isEmpty()) {
                        String direccion = addresses.get(0).getAddressLine(0);
                        txtDireccion.setText(direccion);
                    } else {
                        txtDireccion.setText("Lat: " + latitud + ", Lng: " + longitud);
                    }
                } catch (IOException e) {
                    txtDireccion.setText("Lat: " + latitud + ", Lng: " + longitud);
                }

                Toast.makeText(this, "Ubicación obtenida", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.repBtnTomarFoto)
            tomarFoto();
        else if (v.getId() == R.id.repBtnReportar)
            reportar();
        else if (v.getId() == R.id.repBtnCancelar)
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

    private File createImage() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "JPG_" + timeStamp;
        File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(fileName, ".jpg", directorio);
        sRutaTemporal = imagen.getAbsolutePath();
        return imagen;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK) {
            imgVistaPrevia.setImageURI(uPhoto);
            Toast.makeText(this, "Foto capturada", Toast.LENGTH_SHORT).show();
        } else {
            File tmp = new File(sRutaTemporal);
            tmp.delete();
            Toast.makeText(this, "Foto cancelada", Toast.LENGTH_SHORT).show();
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
            return "";
        }
    }

    private void reportar() {
        String descripcion = txtDescripcion.getText().toString().trim();
        String direccion = txtDireccion.getText().toString().trim();
        String tipo = cboAbusos.getSelectedItem().toString();
        String fechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date());

        if (descripcion.isEmpty() || direccion.isEmpty() || sRutaTemporal == null || sRutaTemporal.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos y toma una foto", Toast.LENGTH_SHORT).show();
            return;
        }

        String base64Foto = convertirImagenABase64(sRutaTemporal);
        SosMujerSqlite db = new SosMujerSqlite(this);
        int usuario_id = db.getUsuarioId();

        RequestParams params = new RequestParams();
        params.put("usuario_id", usuario_id);
        params.put("foto", base64Foto);
        params.put("tipo", tipo);
        params.put("fecha", fechaHora);
        params.put("latitud", latitud);
        params.put("longitud", longitud);
        params.put("lugar", direccion);
        params.put("descripcion", descripcion);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post("http://sos-mujer.atwebpages.com/ws/agregarReporte.php", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(getApplicationContext(), "Reporte enviado correctamente", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), "Error al enviar el reporte", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelar() {
        finish();
    }
}
