package com.tienda.inventario.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.tienda.inventario.database.entities.Categoria;

import java.util.List;

@Dao
public interface CategoriaDao {

    // Insertar categoría
    @Insert
    long insert(Categoria categoria);

    // Actualizar categoría
    @Update
    void update(Categoria categoria);

    // Eliminar categoría
    @Delete
    void delete(Categoria categoria);

    // Obtener todas las categorías
    @Query("SELECT * FROM categorias ORDER BY nombre_categoria ASC")
    LiveData<List<Categoria>> getAllCategorias();

    // Obtener todas las categorías (sin LiveData)
    @Query("SELECT * FROM categorias ORDER BY nombre_categoria ASC")
    List<Categoria> getAllCategoriasList();

    // Obtener categoría por ID
    @Query("SELECT * FROM categorias WHERE id_categoria = :id")
    LiveData<Categoria> getCategoriaById(int id);

    // Obtener categoría por ID (sin LiveData)
    @Query("SELECT * FROM categorias WHERE id_categoria = :id")
    Categoria getCategoriaByIdSync(int id);

    // Buscar categorías por nombre
    @Query("SELECT * FROM categorias WHERE nombre_categoria LIKE '%' || :nombre || '%'")
    LiveData<List<Categoria>> buscarPorNombre(String nombre);

    // Contar productos por categoría
    @Query("SELECT COUNT(*) FROM productos WHERE id_categoria = :idCategoria AND activo = 1")
    LiveData<Integer> contarProductosPorCategoria(int idCategoria);

    // Verificar si existe una categoría con el mismo nombre
    @Query("SELECT COUNT(*) FROM categorias WHERE nombre_categoria = :nombre")
    int existeCategoria(String nombre);

    // Eliminar todas las categorías (para testing)
    @Query("DELETE FROM categorias")
    void deleteAll();

    // Obtener categorías con productos
    @Query("SELECT DISTINCT c.* FROM categorias c " +
            "INNER JOIN productos p ON c.id_categoria = p.id_categoria " +
            "WHERE p.activo = 1 " +
            "ORDER BY c.nombre_categoria ASC")
    LiveData<List<Categoria>> getCategoriasConProductos();

    // Obtener todas las categorías de forma síncrona
    @Query("SELECT * FROM categorias ORDER BY nombre_categoria ASC")
    List<Categoria> getCategoriasSync();

}