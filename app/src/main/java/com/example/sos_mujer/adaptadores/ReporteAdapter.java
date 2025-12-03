package com.example.sos_mujer.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sos_mujer.R;
import com.example.sos_mujer.actividades.DetalleReporteActivity;
import com.example.sos_mujer.clases.Reporte;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import android.location.Location;

public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ViewHolder> {

    private List<Reporte> lista;
    private Context context;

    public static double userLat = 0;
    public static double userLon = 0;

    public ReporteAdapter(List<Reporte> lista, Context ctx) {
        this.lista = lista;
        this.context = ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reporte, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        Reporte r = lista.get(position);

        // IMAGEN O VIDEO
        if (r.getMediaTipo().equals("imagen")) {
            Glide.with(context).load(r.getMediaUrl())
                    .placeholder(R.drawable.previo)
                    .into(holder.imgMedia);
        } else {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(r.getMediaUrl(), new java.util.HashMap<>());
                Bitmap thumb = retriever.getFrameAtTime(0);
                retriever.release();
                holder.imgMedia.setImageBitmap(thumb);
            } catch (Exception e) {
                holder.imgMedia.setImageResource(R.drawable.video_icon);
            }
        }

        // DATOS
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                Locale.getDefault()).format(r.getFecha());

        holder.lblTipo.setText("Tipo: " + r.getTipo());
        holder.lblFecha.setText("Fecha: " + fecha);
        holder.lblLugar.setText("Lugar: " + r.getLugar());
        holder.lblDescripcion.setText("Descripción: " + r.getDescripcion());

        // DISTANCIA
        double distKm = 0;
        if (userLat != 0 && userLon != 0) {
            float[] res = new float[1];
            Location.distanceBetween(userLat, userLon,
                    r.getLatitud(), r.getLongitud(), res);
            distKm = res[0] / 1000.0;

            DecimalFormat df = new DecimalFormat("#0.00");
            holder.lblDistancia.setText("Distancia: " + df.format(distKm) + " km");
        } else {
            holder.lblDistancia.setText("Distancia: --- km");
        }

        double finalDist = distKm;

        // CLICK PARA VER DETALLE
        holder.card.setOnClickListener(v -> {
            Intent i = new Intent(context, DetalleReporteActivity.class);
            i.putExtra("media_url", r.getMediaUrl());
            i.putExtra("media_tipo", r.getMediaTipo());
            i.putExtra("tipo", r.getTipo());
            i.putExtra("fecha", fecha);
            i.putExtra("lugar", r.getLugar());
            i.putExtra("descripcion", r.getDescripcion());
            i.putExtra("distancia", finalDist);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView card;
        ImageView imgMedia;
        TextView lblTipo, lblFecha, lblLugar, lblDescripcion, lblDistancia;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.itmCarReporte);
            imgMedia = itemView.findViewById(R.id.itmImgFoto);
            lblTipo = itemView.findViewById(R.id.itmLblTipo);
            lblFecha = itemView.findViewById(R.id.itmLblFecha);
            lblLugar = itemView.findViewById(R.id.itmLblLugar);
            lblDescripcion = itemView.findViewById(R.id.itmLblDescripcion);
            lblDistancia = itemView.findViewById(R.id.itmLblDistancia);
        }
    }
}
