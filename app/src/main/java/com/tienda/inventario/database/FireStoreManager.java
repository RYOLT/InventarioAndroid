package com.tienda.inventario.database;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tienda.inventario.database.entities.Categoria;
import com.tienda.inventario.database.entities.Producto;
import com.tienda.inventario.database.entities.Proveedor;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreManager {

    private static FirestoreManager instance;
    private final FirebaseFirestore db;

    private FirestoreManager() {
        this.db = FirebaseFirestore.getInstance();
    }

    public FirestoreManager() {
        this.db = db;
    }

    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

    // ==================== PRODUCTOS ====================

    public interface OnProductosListener {
        void onSuccess(List<Producto> productos);
        void onError(String error);
    }

    public void getProductos(OnProductosListener listener) {
        db.collection("productos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Producto> productos = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Producto producto = new Producto();
                        producto.setId(Integer.parseInt(doc.getId()));
                        producto.setNombre(doc.getString("nombre"));
                        producto.setDescripcion(doc.getString("descripcion"));
                        producto.setPrecio(doc.getDouble("precio"));
                        producto.setStock(doc.getLong("stock").intValue());
                        producto.setStockMinimo(doc.getLong("stockMin").intValue());
                        producto.setCodigoBarras(doc.getString("codigoBarras"));

                        productos.add(producto);
                    }
                    listener.onSuccess(productos);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void agregarProducto(Producto producto, OnSuccessListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre", producto.getNombre());
        data.put("descripcion", producto.getDescripcion());
        data.put("precio", producto.getPrecio());
        data.put("stock", producto.getStock());
        data.put("stockMin", producto.getStockMinimo());
        data.put("codigoBarras", producto.getCodigoBarras());
        data.put("timestamp", System.currentTimeMillis());

        db.collection("productos")
                .add(data)
                .addOnSuccessListener(documentReference -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void actualizarProducto(String documentId, Producto producto, OnSuccessListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre", producto.getNombre());
        data.put("descripcion", producto.getDescripcion());
        data.put("precio", producto.getPrecio());
        data.put("stock", producto.getStock());
        data.put("stockMin", producto.getStockMinimo());
        data.put("codigoBarras", producto.getCodigoBarras());

        db.collection("productos")
                .document(documentId)
                .update(data)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void eliminarProducto(String documentId, OnSuccessListener listener) {
        db.collection("productos")
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // ==================== CATEGORÍAS ====================

    public interface OnCategoriasListener {
        void onSuccess(List<Categoria> categorias);
        void onError(String error);
    }

    public void getCategorias(OnCategoriasListener listener) {
        db.collection("categorias")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Categoria> categorias = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Categoria categoria = doc.toObject(Categoria.class);
                        categoria.setId(getId());
                        categorias.add(categoria);
                    }
                    listener.onSuccess(categorias);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    private int getId() {
        return 0;
    }

    // ==================== PROVEEDORES ====================

    public interface OnProveedoresListener {
        void onSuccess(List<Proveedor> proveedores);
        void onError(String error);
    }

    public void getProveedores(OnProveedoresListener listener) {
        db.collection("proveedores")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Proveedor> proveedores = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Proveedor proveedor = doc.toObject(Proveedor.class);
                        proveedor.setId(doc.getId());
                        proveedores.add(proveedor);
                    }
                    listener.onSuccess(proveedores);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // ==================== INTERFACES ====================

    public interface OnSuccessListener {
        void onSuccess();
        void onError(String error);
    }
}