package com.tienda.inventario.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.tienda.inventario.database.entities.Proveedor;

import java.util.List;

@Dao
public interface ProvedoorDao {

    // Insertar proveedor
    @Insert
    long insert(Proveedor proveedor);

    // Actualizar proveedor
    @Update
    void update(Proveedor proveedor);

    // Eliminar proveedor
    @Delete
    void delete(Proveedor proveedor);

    // Obtener todos los proveedores
    @Query("SELECT * FROM proveedores ORDER BY nombre_proveedor ASC")
    LiveData<List<Proveedor>> getAllProveedores();

    // Obtener todos los proveedores (sin LiveData)
    @Query("SELECT * FROM proveedores ORDER BY nombre_proveedor ASC")
    List<Proveedor> getAllProveedoresList();

    // Obtener proveedor por ID
    @Query("SELECT * FROM proveedores WHERE id_proveedor = :id")
    LiveData<Proveedor> getProveedorById(int id);

    // Obtener proveedor por ID (sin LiveData)
    @Query("SELECT * FROM proveedores WHERE id_proveedor = :id")
    Proveedor getProveedorByIdSync(int id);

    // Buscar proveedores por nombre
    @Query("SELECT * FROM proveedores WHERE nombre_proveedor LIKE '%' || :nombre || '%'")
    LiveData<List<Proveedor>> buscarPorNombre(String nombre);

    // Buscar proveedores por ciudad
    @Query("SELECT * FROM proveedores WHERE ciudad LIKE '%' || :ciudad || '%'")
    LiveData<List<Proveedor>> buscarPorCiudad(String ciudad);

    // Buscar proveedores por país
    @Query("SELECT * FROM proveedores WHERE pais = :pais ORDER BY nombre_proveedor ASC")
    LiveData<List<Proveedor>> buscarPorPais(String pais);

    // Contar productos por proveedor
    @Query("SELECT COUNT(*) FROM productos WHERE id_proveedor = :idProveedor AND activo = 1")
    LiveData<Integer> contarProductosPorProveedor(int idProveedor);

    // Verificar si existe un proveedor con el mismo nombre
    @Query("SELECT COUNT(*) FROM proveedores WHERE nombre_proveedor = :nombre")
    int existeProveedor(String nombre);

    // Obtener proveedores con productos
    @Query("SELECT DISTINCT p.* FROM proveedores p " +
            "INNER JOIN productos pr ON p.id_proveedor = pr.id_proveedor " +
            "WHERE pr.activo = 1 " +
            "ORDER BY p.nombre_proveedor ASC")
    LiveData<List<Proveedor>> getProveedoresConProductos();

    // Buscar por email
    @Query("SELECT * FROM proveedores WHERE email = :email")
    Proveedor buscarPorEmail(String email);

    // Eliminar todos los proveedores (para testing)
    @Query("DELETE FROM proveedores")
    void deleteAll();

    // Obtener todos los proveedores de forma síncrona
    @Query("SELECT * FROM proveedores ORDER BY nombre_proveedor ASC")
    List<Proveedor> getProveedorSync();
}