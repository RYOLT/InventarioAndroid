package com.tienda.inventario.database;
/*
public class AppDatabase {
}
*/

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.tienda.inventario.database.dao.CategoriaDao;
import com.tienda.inventario.database.dao.ProductoDao;
import com.tienda.inventario.database.dao.ProvedoorDao;
import com.tienda.inventario.database.entities.Categoria;
import com.tienda.inventario.database.entities.Producto;
import com.tienda.inventario.database.entities.Proveedor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Producto.class, Categoria.class, Proveedor.class},
        version = 1,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // DAOs
    public abstract ProductoDao productoDao();
    public abstract CategoriaDao categoriaDao();
    public abstract ProvedoorDao proveedorDao();

    // Singleton
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    // ExecutorService para operaciones en segundo plano
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "inventario_tienda.db")
                            .addCallback(roomCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Callback para prepoblar la base de datos
//    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
//        @Override
//        public void onCreate(@NonNull SupportSQLiteDatabase db) {
//            super.onCreate(db);
//
//            databaseWriteExecutor.execute(() -> {
//                // Prepoblar con datos iniciales
//                CategoriaDao categoriaDao = INSTANCE.categoriaDao();
//                ProvedoorDao proveedorDao = INSTANCE.proveedorDao();
//
//                // Insertar categorías iniciales
//                Categoria cat1 = new Categoria();
//                cat1.setNombreCategoria("Electrónica");
//                cat1.setDescripcion("Productos electrónicos y tecnología");
//                categoriaDao.insert(cat1);
//
//                Categoria cat2 = new Categoria();
//                cat2.setNombreCategoria("Ropa");
//                cat2.setDescripcion("Prendas de vestir");
//                categoriaDao.insert(cat2);
//
//                Categoria cat3 = new Categoria();
//                cat3.setNombreCategoria("Alimentos");
//                cat3.setDescripcion("Productos alimenticios");
//                categoriaDao.insert(cat3);
//
//                Categoria cat4 = new Categoria();
//                cat4.setNombreCategoria("Hogar");
//                cat4.setDescripcion("Artículos para el hogar");
//                categoriaDao.insert(cat4);
//
//                Categoria cat5 = new Categoria();
//                cat5.setNombreCategoria("Deportes");
//                cat5.setDescripcion("Artículos deportivos");
//                categoriaDao.insert(cat5);
//
//                // Insertar proveedores iniciales
//                Proveedor prov1 = new Proveedor();
//                prov1.setNombreProveedor("TechnoMundo S.A.");
//                prov1.setTelefono("+52 771 123 4567");
//                prov1.setEmail("ventas@technomundo.com");
//                prov1.setDireccion("Av. Tecnológica 100");
//                prov1.setCiudad("Pachuca");
//                prov1.setPais("México");
//                proveedorDao.insert(prov1);
//
//                Proveedor prov2 = new Proveedor();
//                prov2.setNombreProveedor("Distribuidora Global");
//                prov2.setTelefono("+52 771 987 6543");
//                prov2.setEmail("contacto@distglobal.mx");
//                prov2.setDireccion("Blvd. Comercial 250");
//                prov2.setCiudad("Ciudad de México");
//                prov2.setPais("México");
//                proveedorDao.insert(prov2);
//
//                Proveedor prov3 = new Proveedor();
//                prov3.setNombreProveedor("Importadora López");
//                prov3.setTelefono("+52 771 555 1234");
//                prov3.setEmail("info@importadoralopez.com");
//                prov3.setDireccion("Calle Industria 50");
//                prov3.setCiudad("Guadalajara");
//                prov3.setPais("México");
//                proveedorDao.insert(prov3);
//            });
//        }
//    };
    // En AppDatabase.java, modifica el roomCallback:
    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // NOTA: Ya no insertar datos aquí, se cargarán desde Firestore
            Log.d("AppDatabase", "Base de datos creada");
        }
    };
}