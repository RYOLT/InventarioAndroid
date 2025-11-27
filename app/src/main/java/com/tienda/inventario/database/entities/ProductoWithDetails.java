package com.tienda.inventario.database.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

// Clase para representar Producto con sus relaciones (Categoria y Proveedor)
// Room automáticamente hará los JOINs necesarios
public class ProductoWithDetails {

    @Embedded
    public Producto producto;

    @Relation(
            parentColumn = "id_categoria",
            entityColumn = "id_categoria"
    )
    public Categoria categoria;

    @Relation(
            parentColumn = "id_proveedor",
            entityColumn = "id_proveedor"
    )
    public Proveedor proveedor;

    // Constructor vacío
    public ProductoWithDetails() {
    }

    // Getters convenientes
    public String getNombreCategoria() {
        return categoria != null ? categoria.getNombreCategoria() : "Sin categoría";
    }

    public String getNombreProveedor() {
        return proveedor != null ? proveedor.getNombreProveedor() : "Sin proveedor";
    }
}