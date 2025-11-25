package com.example.sos_mujer.adaptadores;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sos_mujer.R;
import com.example.sos_mujer.clases.Reporte;
import com.loopj.android.http.Base64;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ViewHolder> {

    private List<Reporte> lista;

    public static double userLat = 0;
    public static double userLon = 0;

    public ReporteAdapter(List<Reporte> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reporte, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Reporte r = lista.get(position);

        byte[] fotoBytes = Base64.decode(r.getFoto(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.length);
        holder.imgFoto.setImageBitmap(bitmap);

        holder.lblTipo.setText("Tipo: " + r.getTipo());

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        holder.lblFecha.setText("Fecha: " + df.format(r.getFecha()));

        holder.lblLugar.setText("Lugar: " + r.getLugar());
        holder.lblDescripcion.setText("Descripción: " + r.getDescripcion());

        // Distancia
        if (userLat != 0 && userLon != 0) {
            double dist = calcularDistancia(userLat, userLon, r.getLatitud(), r.getLongitud());
            holder.lblDistancia.setText(String.format(Locale.getDefault(), "Distancia: %.2f km", dist));
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        float[] res = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, res);
        return res[0] / 1000;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView carReporte;
        ImageView imgFoto;
        TextView lblTipo, lblFecha, lblLugar, lblDescripcion, lblDistancia;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            carReporte = itemView.findViewById(R.id.itmCarReporte);
            imgFoto = itemView.findViewById(R.id.itmImgFoto);
            lblTipo = itemView.findViewById(R.id.itmLblTipo);
            lblFecha = itemView.findViewById(R.id.itmLblFecha);
            lblLugar = itemView.findViewById(R.id.itmLblLugar);
            lblDescripcion = itemView.findViewById(R.id.itmLblDescripcion);
            lblDistancia = itemView.findViewById(R.id.itmLblDistancia);
        }
    }
}
