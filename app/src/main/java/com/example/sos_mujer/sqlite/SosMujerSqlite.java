package com.example.sos_mujer.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
public class SosMujerSqlite extends SQLiteOpenHelper{

    private final static String NOMBRE_DB = "sosmujer.db";
    private final static int VERSION_DB = 1;
    private final static String CREATE_TABLE_USUARIO = "create table if not exists Usuario(id integer, nombre varchar(100), apellido varchar(100), correo varchar(100), contrasenia varchar(100), dni char(8), telefono char(9), direccion varchar(100), Date fechaNac);";
    private final static String DROP_TABLE_USUARIO = "drop table if exists Usuario;";
    public SosMujerSqlite(@Nullable Context context) {
        super(context, NOMBRE_DB, null, VERSION_DB);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USUARIO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_USUARIO);
        db.execSQL(CREATE_TABLE_USUARIO);
    }

    public boolean agregarUsuario(int id, String nombre, String apellido, String correo, String contrasenia, String dni, String telefono, String direccion, Date fechaNac){
        SQLiteDatabase db = getWritableDatabase();
        if(db != null){
            String sentencia = "insert into Usuario values (?,?,?,?,?,?,?,?,?);";
            Object[] parametros = new Object[]{id, nombre, apellido, correo, contrasenia, dni, telefono, direccion, fechaNac};
            db.execSQL(sentencia, parametros);
            db.close();
            return true;
        }
        return false;
    }

    public boolean recordarSesion(){
        SQLiteDatabase db = getReadableDatabase();
        if(db != null){
            Cursor cursor = db.rawQuery("select id from Usuario", null);
            if(cursor.moveToNext())
                return true;
        }
        return false;
    }

    public String getString(String campo){
        List<String> campos = Arrays.asList("nombre", "apellido", "correo", "contrasenia", "dni", "telefono", "direccion", "fechaNac");
        if(!campos.contains(campo)) return null;
        SQLiteDatabase db = getReadableDatabase();
        if(db != null){
            Cursor cursor = db.rawQuery("Select "+campo+" from Usuario;", null);
            if(cursor.moveToNext())
                return cursor.getString(0);
        }
        return null;
    }

    public boolean actualizarDato(int id, String llave, String valor){
        List<String> campos = Arrays.asList("nombre", "apellido", "correo", "contrasenia", "dni", "telefono", "direccion", "fechaNac");
        if(!campos.contains(llave)) return false;

        SQLiteDatabase db = getWritableDatabase();
        if(db != null){
            String consulta = "update Usuario set "+llave+" = ? where id = ?;";
            Object[] parametros = new Object[]{valor, id};
            db.execSQL(consulta, parametros);
            db.close();
            return true;
        }
        return false;
    }

    public boolean eliminarUsuario(int id){
        SQLiteDatabase db = getWritableDatabase();
        if(db != null){
            String consulta = "delete from Usuario where id = ?;";
            Object[] parametros = new Object[]{id};
            db.execSQL(consulta, parametros);
            db.close();
            return true;
        }
        return false;
    }

    public int getUsuarioId() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Usuario LIMIT 1", null);
        if (cursor.moveToFirst()) {
            return cursor.getInt(0);
        }
        return -1;
    }

    // 🔐 NUEVO: obtener la contraseña (hash SHA-256) guardada
    public String getPassword() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT contrasenia FROM Usuario LIMIT 1", null);
        if (c != null && c.moveToFirst()) {
            return c.getString(0);
        }
        return "";
    }
}
