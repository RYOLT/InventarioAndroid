package com.tienda.inventario.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.tienda.inventario.database.entities.Producto;

import java.util.List;

@Dao
public interface ProductoDao {

    // Insertar producto
    @Insert
    long insert(Producto producto);

    // Actualizar producto
    @Update
    void update(Producto producto);

    // Eliminar producto (físicamente)
    @Delete
    void delete(Producto producto);

    // Soft delete (marcar como inactivo)
    @Query("UPDATE productos SET activo = 0 WHERE id_producto = :idProducto")
    void softDelete(int idProducto);

    // Obtener todos los productos activos
    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre_producto ASC")
    LiveData<List<Producto>> getAllProductos();

    // Obtener producto por ID
    @Query("SELECT * FROM productos WHERE id_producto = :id")
    LiveData<Producto> getProductoById(int id);

    // Buscar productos por nombre
    @Query("SELECT * FROM productos WHERE nombre_producto LIKE '%' || :nombre || '%' AND activo = 1")
    LiveData<List<Producto>> searchByName(String nombre);

    // Obtener productos por categoría
    @Query("SELECT * FROM productos WHERE id_categoria = :idCategoria AND activo = 1")
    LiveData<List<Producto>> getProductosByCategoria(int idCategoria);

    // Obtener productos con stock bajo
    @Query("SELECT * FROM productos WHERE stock_actual <= stock_minimo AND activo = 1 ORDER BY stock_actual ASC")
    LiveData<List<Producto>> getProductosStockBajo();

    // Buscar por código de barras
    @Query("SELECT * FROM productos WHERE codigo_barras = :codigoBarras AND activo = 1 LIMIT 1")
    Producto findByCodigoBarras(String codigoBarras);

    // Actualizar stock
    @Query("UPDATE productos SET stock_actual = :nuevoStock, ultima_actualizacion = :timestamp WHERE id_producto = :idProducto")
    void updateStock(int idProducto, int nuevoStock, long timestamp);

    // Contar productos activos
    @Query("SELECT COUNT(*) FROM productos WHERE activo = 1")
    LiveData<Integer> countProductosActivos();

    // Obtener valor total del inventario
    @Query("SELECT SUM(precio_unitario * stock_actual) FROM productos WHERE activo = 1")
    LiveData<Double> getValorTotalInventario();

    // Obtener productos por proveedor
    @Query("SELECT * FROM productos WHERE id_proveedor = :idProveedor AND activo = 1")
    LiveData<List<Producto>> getProductosByProveedor(int idProveedor);
}