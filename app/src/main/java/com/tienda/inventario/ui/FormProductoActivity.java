package com.tienda.inventario.ui;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tienda.inventario.database.entities.Categoria;
import com.tienda.inventario.database.entities.Producto;
import com.tienda.inventario.database.entities.Proveedor;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormProductoActivity {

    private static FirestoreManager instance;
    private final FirebaseFirestore db;

    private FirestoreManager() {
        db = FirebaseFirestore.getInstance();
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
                        try {
                            Producto producto = new Producto();

                            // Usar el ID del documento de Firestore
                            producto.setIdProducto(doc.getId().hashCode());

                            producto.setNombreProducto(doc.getString("nombre"));
                            producto.setDescripcion(doc.getString("descripcion"));

                            // Manejar precio de forma segura
                            Object precioObj = doc.get("precio");
                            if (precioObj instanceof Double) {
                                producto.setPrecioUnitario((Double) precioObj);
                            } else if (precioObj instanceof Long) {
                                producto.setPrecioUnitario(((Long) precioObj).doubleValue());
                            }

                            // Manejar stock de forma segura
                            Object stockObj = doc.get("stock");
                            if (stockObj instanceof Long) {
                                producto.setStockActual(((Long) stockObj).intValue());
                            } else if (stockObj instanceof Double) {
                                producto.setStockActual(((Double) stockObj).intValue());
                            }

                            // Manejar stock mínimo de forma segura
                            Object stockMinObj = doc.get("stockMin");
                            if (stockMinObj instanceof Long) {
                                producto.setStockMinimo(((Long) stockMinObj).intValue());
                            } else if (stockMinObj instanceof Double) {
                                producto.setStockMinimo(((Double) stockMinObj).intValue());
                            }

                            producto.setCodigoBarras(doc.getString("codigoBarras"));

                            // Manejar IDs de categoría y proveedor
                            Object catIdObj = doc.get("idCategoria");
                            if (catIdObj instanceof Long) {
                                producto.setIdCategoria(((Long) catIdObj).intValue());
                            } else if (catIdObj instanceof Double) {
                                producto.setIdCategoria(((Double) catIdObj).intValue());
                            } else {
                                producto.setIdCategoria(1); // Default
                            }

                            Object provIdObj = doc.get("idProveedor");
                            if (provIdObj instanceof Long) {
                                producto.setIdProveedor(((Long) provIdObj).intValue());
                            } else if (provIdObj instanceof Double) {
                                producto.setIdProveedor(((Double) provIdObj).intValue());
                            } else {
                                producto.setIdProveedor(1); // Default
                            }

                            productos.add(producto);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    listener.onSuccess(productos);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void agregarProducto(Producto producto, OnSuccessListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre", producto.getNombreProducto());
        data.put("descripcion", producto.getDescripcion());
        data.put("precio", producto.getPrecioUnitario());
        data.put("stock", producto.getStockActual());
        data.put("stockMin", producto.getStockMinimo());
        data.put("codigoBarras", producto.getCodigoBarras());
        data.put("idCategoria", producto.getIdCategoria());
        data.put("idProveedor", producto.getIdProveedor());
        data.put("timestamp", System.currentTimeMillis());

        db.collection("productos")
                .add(data)
                .addOnSuccessListener(documentReference -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void actualizarProducto(String documentId, Producto producto, OnSuccessListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre", producto.getNombreProducto());
        data.put("descripcion", producto.getDescripcion());
        data.put("precio", producto.getPrecioUnitario());
        data.put("stock", producto.getStockActual());
        data.put("stockMin", producto.getStockMinimo());
        data.put("codigoBarras", producto.getCodigoBarras());
        data.put("idCategoria", producto.getIdCategoria());
        data.put("idProveedor", producto.getIdProveedor());

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
                        Categoria categoria = new Categoria();
                        categoria.setIdCategoria(doc.getId().hashCode());
                        categoria.setNombreCategoria(doc.getString("nombre"));
                        categoria.setDescripcion(doc.getString("descripcion"));
                        categorias.add(categoria);
                    }
                    listener.onSuccess(categorias);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
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
                        Proveedor proveedor = new Proveedor();
                        proveedor.setIdProveedor(doc.getId().hashCode());
                        proveedor.setNombreProveedor(doc.getString("nombre"));
                        proveedor.setTelefono(doc.getString("telefono"));
                        proveedor.setEmail(doc.getString("email"));
                        proveedor.setDireccion(doc.getString("direccion"));
                        proveedor.setCiudad(doc.getString("ciudad"));
                        proveedor.setPais(doc.getString("pais"));
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