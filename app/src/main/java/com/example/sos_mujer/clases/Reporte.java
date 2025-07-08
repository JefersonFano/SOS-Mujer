package com.example.sos_mujer.clases;
import java.util.Date;

public class Reporte {
    private int id;
    private String foto;
    private String tipo;
    private Date fecha;
    private String lugar;
    private String descripcion;

    public Reporte() {
    }

    public Reporte(int id, String foto, String tipo, Date fecha, String lugar, String descripcion) {
        this.id = id;
        this.foto = foto;
        this.tipo = tipo;
        this.fecha = fecha;
        this.lugar = lugar;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
