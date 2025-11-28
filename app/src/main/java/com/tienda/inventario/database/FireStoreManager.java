package com.tienda.inventario.database;

import android.util.Log;

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

    private static final String TAG = "FirestoreManager";
    private static FirestoreManager instance;
    private final FirebaseFirestore db;

    private FirestoreManager() {
        this.db = FirebaseFirestore.getInstance();
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

                            // Usar el docId de Firestore como ID
                            producto.setDocId(doc.getId());

                            // ID del producto (usar hashCode del docId para mantener consistencia)
                            producto.setIdProducto(doc.getId().hashCode());

                            // Nombre del producto
                            String nombre = doc.getString("nombre_producto");
                            if (nombre == null)
                                nombre = doc.getString("nombre");
                            producto.setNombreProducto(nombre);

                            // Descripción
                            String descripcion = doc.getString("descripcion");
                            producto.setDescripcion(descripcion != null ? descripcion : "");

                            // Precio
                            Object precioObj = doc.get("precio_unitario");
                            if (precioObj == null)
                                precioObj = doc.get("precio");
                            if (precioObj instanceof Double) {
                                producto.setPrecioUnitario((Double) precioObj);
                            } else if (precioObj instanceof Long) {
                                producto.setPrecioUnitario(((Long) precioObj).doubleValue());
                            }

                            // Stock actual
                            Object stockObj = doc.get("stock_actual");
                            if (stockObj == null)
                                stockObj = doc.get("stock");
                            if (stockObj instanceof Long) {
                                producto.setStockActual(((Long) stockObj).intValue());
                            } else if (stockObj instanceof Double) {
                                producto.setStockActual(((Double) stockObj).intValue());
                            }

                            // Stock mínimo
                            Object stockMinObj = doc.get("stock_minimo");
                            if (stockMinObj == null)
                                stockMinObj = doc.get("stockMin");
                            if (stockMinObj instanceof Long) {
                                producto.setStockMinimo(((Long) stockMinObj).intValue());
                            } else if (stockMinObj instanceof Double) {
                                producto.setStockMinimo(((Double) stockMinObj).intValue());
                            }

                            // Código de barras
                            String codigoBarras = doc.getString("codigo_barras");
                            if (codigoBarras == null)
                                codigoBarras = doc.getString("codigoBarras");
                            producto.setCodigoBarras(codigoBarras != null ? codigoBarras : "");

                            // ID de categoría
                            Object catIdObj = doc.get("id_categoria");
                            if (catIdObj == null)
                                catIdObj = doc.get("idCategoria");
                            if (catIdObj instanceof Long) {
                                producto.setIdCategoria(((Long) catIdObj).intValue());
                            } else if (catIdObj instanceof Double) {
                                producto.setIdCategoria(((Double) catIdObj).intValue());
                            } else if (catIdObj instanceof String) {
                                try {
                                    producto.setIdCategoria(Integer.parseInt((String) catIdObj));
                                } catch (NumberFormatException e) {
                                    producto.setIdCategoria(1);
                                }
                            } else {
                                producto.setIdCategoria(1);
                            }

                            // ID de proveedor
                            Object provIdObj = doc.get("id_proveedor");
                            if (provIdObj == null)
                                provIdObj = doc.get("idProveedor");
                            if (provIdObj instanceof Long) {
                                producto.setIdProveedor(((Long) provIdObj).intValue());
                            } else if (provIdObj instanceof Double) {
                                producto.setIdProveedor(((Double) provIdObj).intValue());
                            } else if (provIdObj instanceof String) {
                                try {
                                    producto.setIdProveedor(Integer.parseInt((String) provIdObj));
                                } catch (NumberFormatException e) {
                                    producto.setIdProveedor(1);
                                }
                            } else {
                                producto.setIdProveedor(1);
                            }

                            // Activo
                            Boolean activo = doc.getBoolean("activo");
                            producto.setActivo(activo != null ? activo : true);

                            productos.add(producto);
                            Log.d(TAG, "Producto cargado: " + producto.getNombreProducto());
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar producto: " + e.getMessage(), e);
                        }
                    }
                    Log.d(TAG, "Total productos cargados: " + productos.size());
                    listener.onSuccess(productos);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar productos: " + e.getMessage(), e);
                    listener.onError(e.getMessage());
                });
    }

    public void agregarProducto(Producto producto, OnSuccessListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre_producto", producto.getNombreProducto());
        data.put("descripcion", producto.getDescripcion());
        data.put("precio_unitario", producto.getPrecioUnitario());
        data.put("stock_actual", producto.getStockActual());
        data.put("stock_minimo", producto.getStockMinimo());
        data.put("codigo_barras", producto.getCodigoBarras());
        data.put("id_categoria", producto.getIdCategoria());
        data.put("id_proveedor", producto.getIdProveedor());
        data.put("activo", true);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("productos")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Producto agregado con ID: " + documentReference.getId());
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al agregar producto: " + e.getMessage(), e);
                    listener.onError(e.getMessage());
                });
    }

    public void actualizarProducto(String documentId, Producto producto, OnSuccessListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre_producto", producto.getNombreProducto());
        data.put("descripcion", producto.getDescripcion());
        data.put("precio_unitario", producto.getPrecioUnitario());
        data.put("stock_actual", producto.getStockActual());
        data.put("stock_minimo", producto.getStockMinimo());
        data.put("codigo_barras", producto.getCodigoBarras());
        data.put("id_categoria", producto.getIdCategoria());
        data.put("id_proveedor", producto.getIdProveedor());
        data.put("timestamp", System.currentTimeMillis());

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
                        try {
                            Categoria categoria = new Categoria();
                            categoria.setIdCategoria(doc.getId().hashCode());

                            String nombre = doc.getString("nombre_categoria");
                            if (nombre == null)
                                nombre = doc.getString("nombre");
                            categoria.setNombreCategoria(nombre);

                            String descripcion = doc.getString("descripcion");
                            categoria.setDescripcion(descripcion != null ? descripcion : "");

                            categorias.add(categoria);
                            Log.d(TAG, "Categoría cargada: " + categoria.getNombreCategoria());
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar categoría: " + e.getMessage(), e);
                        }
                    }
                    Log.d(TAG, "Total categorías cargadas: " + categorias.size());
                    listener.onSuccess(categorias);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar categorías: " + e.getMessage(), e);
                    listener.onError(e.getMessage());
                });
    }

    public void agregarCategoria(Categoria categoria, OnSuccessListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre_categoria", categoria.getNombreCategoria());
        data.put("descripcion", categoria.getDescripcion());
        data.put("timestamp", System.currentTimeMillis());

        db.collection("categorias")
                .add(data)
                .addOnSuccessListener(documentReference -> listener.onSuccess())
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
                        try {
                            Proveedor proveedor = new Proveedor();
                            proveedor.setIdProveedor(doc.getId().hashCode());

                            String nombre = doc.getString("nombre_proveedor");
                            if (nombre == null)
                                nombre = doc.getString("nombre");
                            proveedor.setNombreProveedor(nombre);

                            proveedor.setTelefono(doc.getString("telefono"));
                            proveedor.setEmail(doc.getString("email"));
                            proveedor.setDireccion(doc.getString("direccion"));
                            proveedor.setCiudad(doc.getString("ciudad"));
                            proveedor.setPais(doc.getString("pais"));

                            proveedores.add(proveedor);
                            Log.d(TAG, "Proveedor cargado: " + proveedor.getNombreProveedor());
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar proveedor: " + e.getMessage(), e);
                        }
                    }
                    Log.d(TAG, "Total proveedores cargados: " + proveedores.size());
                    listener.onSuccess(proveedores);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar proveedores: " + e.getMessage(), e);
                    listener.onError(e.getMessage());
                });
    }

    public void agregarProveedor(Proveedor proveedor, OnSuccessListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre_proveedor", proveedor.getNombreProveedor());
        data.put("telefono", proveedor.getTelefono());
        data.put("email", proveedor.getEmail());
        data.put("direccion", proveedor.getDireccion());
        data.put("ciudad", proveedor.getCiudad());
        data.put("pais", proveedor.getPais());
        data.put("timestamp", System.currentTimeMillis());

        db.collection("proveedores")
                .add(data)
                .addOnSuccessListener(documentReference -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // ==================== INTERFACES ====================

    public interface OnSuccessListener {
        void onSuccess();

        void onError(String error);
    }
}