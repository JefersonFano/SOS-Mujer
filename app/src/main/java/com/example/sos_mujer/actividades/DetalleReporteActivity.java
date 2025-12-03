package com.example.sos_mujer.actividades;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.MediaController;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.sos_mujer.R;

import java.util.Locale;

public class DetalleReporteActivity extends AppCompatActivity {

    ImageView imgDetalle;
    VideoView vidDetalle;
    TextView txtTipo, txtFecha, txtLugar, txtDescripcion, txtDistancia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_reporte);

        imgDetalle = findViewById(R.id.detImg);
        vidDetalle = findViewById(R.id.detVideo);
        txtTipo = findViewById(R.id.detTipo);
        txtFecha = findViewById(R.id.detFecha);
        txtLugar = findViewById(R.id.detLugar);
        txtDescripcion = findViewById(R.id.detDescripcion);
        txtDistancia = findViewById(R.id.detDistancia);

        String mediaUrl = getIntent().getStringExtra("media_url");
        String mediaTipo = getIntent().getStringExtra("media_tipo");
        String tipo = getIntent().getStringExtra("tipo");
        String fecha = getIntent().getStringExtra("fecha");
        String lugar = getIntent().getStringExtra("lugar");
        String descripcion = getIntent().getStringExtra("descripcion");
        double distancia = getIntent().getDoubleExtra("distancia", 0);

        txtTipo.setText(tipo);
        txtFecha.setText(fecha);
        txtLugar.setText(lugar);
        txtDescripcion.setText(descripcion);
        txtDistancia.setText(String.format(Locale.getDefault(),
                "Distancia: %.2f km", distancia));

        if (mediaTipo.equals("imagen")) {

            imgDetalle.setVisibility(ImageView.VISIBLE);
            vidDetalle.setVisibility(VideoView.GONE);

            Glide.with(this)
                    .load(mediaUrl)
                    .into(imgDetalle);

        } else {
            imgDetalle.setVisibility(ImageView.GONE);
            vidDetalle.setVisibility(VideoView.VISIBLE);

            vidDetalle.setVideoURI(Uri.parse(mediaUrl));
            MediaController mediaController = new MediaController(this);
            vidDetalle.setMediaController(mediaController);
            mediaController.setAnchorView(vidDetalle);
            vidDetalle.start();
        }
    }
}
