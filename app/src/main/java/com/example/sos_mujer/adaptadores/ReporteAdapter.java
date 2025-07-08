package com.example.sos_mujer.adaptadores;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.loopj.android.http.Base64;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ViewHolder> {
    private List<Reporte> listaReporte;

    public ReporteAdapter(List<Reporte> listaReporte) {
        this.listaReporte = listaReporte;
    }

    @NonNull
    @Override
    public ReporteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reporte, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ReporteAdapter.ViewHolder holder, int position) {
        Reporte reporte = listaReporte.get(position);
        String foto = reporte.getFoto();
        byte[] fotoByte = Base64.decode(foto, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(fotoByte, 0, fotoByte.length);
        holder.imgFoto.setImageBitmap(bitmap);
        holder.lblTipo.setText("Tipo: " + reporte.getTipo());
        Date fecha = reporte.getFecha();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        holder.lblFecha.setText("Fecha: " + format.format(fecha));
        holder.lblLugar.setText("Lugar: " + reporte.getLugar());
        holder.lblDescripcion.setText("Descripción: " + reporte.getDescripcion());
    }

    @Override
    public int getItemCount() {
        return listaReporte.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        CardView carReporte;
        ImageView imgFoto;
        TextView lblTipo, lblFecha, lblLugar, lblDescripcion;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            carReporte = itemView.findViewById(R.id.itmCarReporte);
            imgFoto = itemView.findViewById(R.id.itmImgFoto);
            lblTipo = itemView.findViewById(R.id.itmLblTipo);
            lblFecha = itemView.findViewById(R.id.itmLblFecha);
            lblLugar = itemView.findViewById(R.id.itmLblLugar);
            lblDescripcion = itemView.findViewById(R.id.itmLblDescripcion);
        }
    }
}
