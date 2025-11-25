package com.example.sos_mujer.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sos_mujer.R;
import com.example.sos_mujer.clases.Contacto;

import java.util.List;

public class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.ViewHolder> {

    public interface ContactoListener {
        void editar(Contacto contacto);
        void eliminar(Contacto contacto);
    }

    private List<Contacto> lista;
    private ContactoListener listener;

    public ContactoAdapter(List<Contacto> lista, ContactoListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contacto, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contacto c = lista.get(position);

        holder.nombre.setText(c.nombre);
        holder.numero.setText(c.numero);

        holder.btnEditar.setOnClickListener(v -> listener.editar(c));
        holder.btnEliminar.setOnClickListener(v -> listener.eliminar(c));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nombre, numero;
        Button btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.contactoNombre);
            numero = itemView.findViewById(R.id.contactoNumero);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}
