package com.tienda.inventario.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.tienda.inventario.database.entities.Producto;
import com.tienda.inventario.repository.ProductoRepository;

import java.util.List;

public class ProductoViewModel extends AndroidViewModel {

    private ProductoRepository repository;
    private LiveData<List<Producto>> allProductos;

    public ProductoViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductoRepository(application);
        allProductos = repository.getAllProductos();
    }

    // Obtener todos los productos
    public LiveData<List<Producto>> getAllProductos() {
        return allProductos;
    }

    // Obtener productos con stock bajo
    public LiveData<List<Producto>> getProductosStockBajo() {
        return repository.getProductosStockBajo();
    }

    // Buscar productos
    public LiveData<List<Producto>> searchByName(String nombre) {
        return repository.searchByName(nombre);
    }

    // Obtener producto por ID
    public LiveData<Producto> getProductoById(int id) {
        return repository.getProductoById(id);
    }

    // Insertar producto
    public void insert(Producto producto) {
        repository.insert(producto);
    }

    // Actualizar producto
    public void update(Producto producto) {
        repository.update(producto);
    }

    // Eliminar producto
    public void delete(int idProducto) {
        repository.delete(idProducto);
    }

    // Actualizar stock
    public void updateStock(int idProducto, int nuevoStock) {
        repository.updateStock(idProducto, nuevoStock);
    }

    // Obtener estadísticas
    public LiveData<Integer> countProductosActivos() {
        return repository.countProductosActivos();
    }

    public LiveData<Double> getValorTotalInventario() {
        return repository.getValorTotalInventario();
    }
}