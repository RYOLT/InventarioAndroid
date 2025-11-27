package com.tienda.inventario.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias")
public class Categoria {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_categoria")
    private int idCategoria;

    @ColumnInfo(name = "nombre_categoria")
    private String nombreCategoria;

    private String descripcion;

    @ColumnInfo(name = "fecha_creacion")
    private long fechaCreacion;

    // Constructor vacío
    public Categoria() {
        this.fechaCreacion = System.currentTimeMillis();
    }

    // Constructor con parámetros
    @Ignore
    public Categoria(String nombreCategoria, String descripcion) {
        this.nombreCategoria = nombreCategoria;
        this.descripcion = descripcion;
        this.fechaCreacion = System.currentTimeMillis();
    }

    // Getters y Setters
    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public long getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public String toString() {
        return nombreCategoria; // Para mostrar en Spinners
    }

    public void setId(int i) {
    }

    public void setNombre(String nombre) {
    }
}