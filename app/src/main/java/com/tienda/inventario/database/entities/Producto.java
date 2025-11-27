package com.tienda.inventario.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "productos",
        foreignKeys = {
                @ForeignKey(
                        entity = Categoria.class,
                        parentColumns = "id_categoria",
                        childColumns = "id_categoria",
                        onDelete = ForeignKey.RESTRICT
                ),
                @ForeignKey(
                        entity = Proveedor.class,
                        parentColumns = "id_proveedor",
                        childColumns = "id_proveedor",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index("id_categoria"),
                @Index("id_proveedor"),
                @Index("codigo_barras")
        })
public class Producto {



    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_producto")
    private int idProducto;

    @ColumnInfo(name = "nombre_producto")
    private String nombreProducto;

    private String descripcion;

    @ColumnInfo(name = "precio_unitario")
    private double precioUnitario;

    @ColumnInfo(name = "stock_actual")
    private int stockActual;

    @ColumnInfo(name = "stock_minimo")
    private int stockMinimo;

    @ColumnInfo(name = "id_categoria")
    private int idCategoria;

    @ColumnInfo(name = "id_proveedor")
    private int idProveedor;

    @ColumnInfo(name = "codigo_barras")
    private String codigoBarras;

    @ColumnInfo(name = "fecha_registro")
    private long fechaRegistro;

    @ColumnInfo(name = "ultima_actualizacion")
    private long ultimaActualizacion;

    private boolean activo;

    // Constructor vacío (requerido por Room)
    public Producto() {
        this.activo = true;
        this.fechaRegistro = System.currentTimeMillis();
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    // Getters y Setters
    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public long getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(long fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public long getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(long ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Método útil para verificar stock bajo
    public boolean isBajoStock() {
        return stockActual <= stockMinimo;
    }
}