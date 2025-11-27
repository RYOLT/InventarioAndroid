package com.tienda.inventario.repository;
/*
public class ProductoRepository {
}
*/

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.tienda.inventario.database.AppDatabase;
import com.tienda.inventario.database.dao.ProductoDao;
import com.tienda.inventario.database.entities.Producto;

import java.util.List;

public class ProductoRepository {

    private ProductoDao productoDao;
    private LiveData<List<Producto>> allProductos;

    public ProductoRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        productoDao = database.productoDao();
        allProductos = productoDao.getAllProductos();
    }

    // Obtener todos los productos
    public LiveData<List<Producto>> getAllProductos() {
        return allProductos;
    }

    // Obtener productos con stock bajo
    public LiveData<List<Producto>> getProductosStockBajo() {
        return productoDao.getProductosStockBajo();
    }

    // Buscar productos por nombre
    public LiveData<List<Producto>> searchByName(String nombre) {
        return productoDao.searchByName(nombre);
    }

    // Obtener producto por ID
    public LiveData<Producto> getProductoById(int id) {
        return productoDao.getProductoById(id);
    }

    // Insertar producto
    public void insert(Producto producto) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productoDao.insert(producto);
        });
    }

    // Actualizar producto
    public void update(Producto producto) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            producto.setUltimaActualizacion(System.currentTimeMillis());
            productoDao.update(producto);
        });
    }

    // Eliminar producto (soft delete)
    public void delete(int idProducto) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productoDao.softDelete(idProducto);
        });
    }

    // Actualizar stock
    public void updateStock(int idProducto, int nuevoStock) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productoDao.updateStock(idProducto, nuevoStock, System.currentTimeMillis());
        });
    }

    // Obtener conteo de productos
    public LiveData<Integer> countProductosActivos() {
        return productoDao.countProductosActivos();
    }

    // Obtener valor total del inventario
    public LiveData<Double> getValorTotalInventario() {
        return productoDao.getValorTotalInventario();
    }
}