package com.example.sos_mujer.actividades;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaMetadataRetriever;
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
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class ReportarActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    // 🔹 Configuración Cloudinary
    private static final String CLOUDINARY_CLOUD_NAME = "dw4impeon";
    private static final String CLOUDINARY_UPLOAD_PRESET = "sos_mujer_unsigned";

    EditText txtDescripcion, txtDireccion;
    Spinner cboAbusos;
    ImageButton btnTomarMedia;
    ImageView imgVistaPrevia;
    Button btnReportar, btnCancelar, btnUbicacion;

    Uri mediaUri = null;
    String mediaTipo = ""; // "imagen" o "video"

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

        btnTomarMedia = findViewById(R.id.repBtnTomarFoto);
        imgVistaPrevia = findViewById(R.id.imgVistaPrevia);
        btnReportar = findViewById(R.id.repBtnReportar);
        btnCancelar = findViewById(R.id.repBtnCancelar);
        btnUbicacion = findViewById(R.id.repBtnUbicacionActual);
        txtDescripcion = findViewById(R.id.repTxtDescripcion);
        txtDireccion = findViewById(R.id.repTxtDireccion);
        cboAbusos = findViewById(R.id.repCboAbuso);

        btnTomarMedia.setOnClickListener(this);
        btnReportar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        btnUbicacion.setOnClickListener(v -> obtenerUbicacion());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        validarPermisos();
    }

    private void validarPermisos() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 1000);
    }

    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);

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
                        txtDireccion.setText(addresses.get(0).getAddressLine(0));
                    } else {
                        txtDireccion.setText("Lat: " + latitud + ", Lng: " + longitud);
                    }

                } catch (Exception e) {
                    txtDireccion.setText("Lat: " + latitud + ", Lng: " + longitud);
                }

                Toast.makeText(this, "Ubicación obtenida", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.repBtnTomarFoto)
            mostrarOpcionesMedia();

        else if (v.getId() == R.id.repBtnReportar)
            reportar();

        else if (v.getId() == R.id.repBtnCancelar)
            finish();
    }

    private void mostrarOpcionesMedia() {

        String[] opciones = {"Tomar foto", "Elegir de galería (foto/video)"};

        new AlertDialog.Builder(this)
                .setTitle("Seleccionar medio")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) abrirCamara();
                    else abrirGaleria();
                }).show();
    }

    private void abrirCamara() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            uPhoto = FileProvider.getUriForFile(
                    this, "com.example.sos_mujer.provider", createImageFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uPhoto);
            startActivityForResult(intent, REQUEST_CAMERA);

        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp;

        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(fileName, ".jpg", dir);

        sRutaTemporal = image.getAbsolutePath();
        return image;
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "Operación cancelada", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == REQUEST_CAMERA) { // Foto cámara

            mediaUri = uPhoto;
            mediaTipo = "imagen";
            imgVistaPrevia.setImageURI(mediaUri);

        } else if (requestCode == REQUEST_GALLERY && data != null) {

            mediaUri = data.getData();
            String mime = getContentResolver().getType(mediaUri);

            if (mime != null && mime.startsWith("video")) {

                mediaTipo = "video";

                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(this, mediaUri);
                    Bitmap thumb = retriever.getFrameAtTime(0);
                    retriever.release();

                    imgVistaPrevia.setImageBitmap(thumb);

                } catch (Exception e) {
                    imgVistaPrevia.setImageResource(R.drawable.previo);
                }

            } else {
                mediaTipo = "imagen";
                imgVistaPrevia.setImageURI(mediaUri);
            }
        }
    }

    // ================================
    //    🚀 SUBIR ARCHIVO A CLOUDINARY
    // ================================
    private void subirMediaACloudinary(String descripcion, String direccion,
                                       String tipo, String fechaHora) {

        try {
            String resourceType = mediaTipo.equals("video") ? "video" : "image";

            String url = "https://api.cloudinary.com/v1_1/"
                    + CLOUDINARY_CLOUD_NAME + "/" + resourceType + "/upload";

            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();

            params.put("upload_preset", CLOUDINARY_UPLOAD_PRESET);

            // Usar InputStream desde el Uri
            InputStream is = getContentResolver().openInputStream(mediaUri);
            if (is == null) {
                Toast.makeText(this, "No se pudo abrir el archivo", Toast.LENGTH_SHORT).show();
                return;
            }

            String fileName = "rep_" + System.currentTimeMillis();
            params.put("file", is, fileName);

            client.post(url, params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                                      JSONObject response) {

                    try {
                        String mediaUrl = response.getString("secure_url");
                        enviarReporteABackend(mediaUrl, mediaTipo,
                                descripcion, direccion, tipo, fechaHora);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ReportarActivity.this,
                                "Error al leer respuesta de Cloudinary",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      Throwable throwable,
                                      JSONObject errorResponse) {

                    Toast.makeText(ReportarActivity.this,
                            "Error subiendo archivo a Cloudinary",
                            Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error preparando archivo para subir",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarReporteABackend(String mediaUrl, String mediaTipo,
                                       String descripcion, String direccion,
                                       String tipo, String fechaHora) {

        SosMujerSqlite db = new SosMujerSqlite(this);
        int usuario_id = db.getUsuarioId();

        RequestParams params = new RequestParams();
        params.put("usuario_id", usuario_id);
        params.put("media_url", mediaUrl);
        params.put("media_tipo", mediaTipo);
        params.put("tipo", tipo);
        params.put("fecha", fechaHora);
        params.put("latitud", latitud);
        params.put("longitud", longitud);
        params.put("lugar", direccion);
        params.put("descripcion", descripcion);

        AsyncHttpClient client = new AsyncHttpClient();

        client.post("http://sos-mujer.atwebpages.com/ws/agregarReporte.php",
                params, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          JSONObject response) {
                        Toast.makeText(getApplicationContext(),
                                "Reporte enviado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          Throwable throwable,
                                          JSONObject errorResponse) {
                        Toast.makeText(getApplicationContext(),
                                "Error al enviar reporte", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================================
    //  🚀 MÉTODO PRINCIPAL REPORTAR
    // ================================
    private void reportar() {

        String descripcion = txtDescripcion.getText().toString().trim();
        String direccion = txtDireccion.getText().toString().trim();
        String tipo = cboAbusos.getSelectedItem().toString();
        String fechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.ENGLISH).format(new Date());

        if (descripcion.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this, "Completa descripción y dirección", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mediaUri == null || mediaTipo.isEmpty()) {
            Toast.makeText(this, "Debes seleccionar imagen o video", Toast.LENGTH_SHORT).show();
            return;
        }

        subirMediaACloudinary(descripcion, direccion, tipo, fechaHora);
    }
}
