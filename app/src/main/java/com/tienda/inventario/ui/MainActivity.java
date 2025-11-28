package com.tienda.inventario.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tienda.inventario.R;
import com.tienda.inventario.database.AppDatabase;
import com.tienda.inventario.database.entities.Categoria;
import com.tienda.inventario.database.entities.Producto;
import com.tienda.inventario.database.entities.Proveedor;
import com.tienda.inventario.databinding.ActivityMainBinding;
import com.tienda.inventario.database.FirestoreManager;
import com.tienda.inventario.ui.adapter.FormProductoActivity;
import com.tienda.inventario.ui.adapter.ProductoAdapter;
import com.tienda.inventario.viewmodel.ProductoViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirestoreManager firestoreManager;
    private ActivityMainBinding binding;
    private ProductoViewModel viewModel;
    private ProductoAdapter adapter;
    private List<Producto> listaProductos = new ArrayList<>();
    private boolean primeraSync = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d(TAG, "onCreate iniciado");

        // Inicializar Firestore Manager
        firestoreManager = FirestoreManager.getInstance();

        // Configurar Toolbar
        setSupportActionBar(binding.toolbar);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(ProductoViewModel.class);

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar listeners
        setupListeners();

        // Primero cargar categorías y proveedores, luego productos
        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        binding.progressBar.setVisibility(View.VISIBLE);

        // Cargar categorías primero
        firestoreManager.getCategorias(new FirestoreManager.OnCategoriasListener() {
            @Override
            public void onSuccess(List<Categoria> categorias) {
                Log.d(TAG, "Categorías cargadas: " + categorias.size());
                guardarCategoriasEnRoom(categorias);

                // Luego cargar proveedores
                firestoreManager.getProveedores(new FirestoreManager.OnProveedoresListener() {
                    @Override
                    public void onSuccess(List<Proveedor> proveedores) {
                        Log.d(TAG, "Proveedores cargados: " + proveedores.size());
                        guardarProveedoresEnRoom(proveedores);

                        // Finalmente cargar productos
                        cargarProductosFirestore();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error al cargar proveedores: " + error);
                        // Continuar cargando productos aunque fallen proveedores
                        cargarProductosFirestore();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al cargar categorías: " + error);
                // Continuar cargando productos aunque fallen categorías
                cargarProductosFirestore();
            }
        });
    }

    private void guardarCategoriasEnRoom(List<Categoria> categorias) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                for (Categoria categoria : categorias) {
                    AppDatabase.getDatabase(MainActivity.this)
                            .categoriaDao()
                            .insert(categoria);
                }
                Log.d(TAG, "Categorías guardadas en Room");
            } catch (Exception e) {
                Log.e(TAG, "Error al guardar categorías en Room: " + e.getMessage());
            }
        });
    }

    private void guardarProveedoresEnRoom(List<Proveedor> proveedores) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                for (Proveedor proveedor : proveedores) {
                    AppDatabase.getDatabase(MainActivity.this)
                            .proveedorDao()
                            .insert(proveedor);
                }
                Log.d(TAG, "Proveedores guardados en Room");
            } catch (Exception e) {
                Log.e(TAG, "Error al guardar proveedores en Room: " + e.getMessage());
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ProductoAdapter();
        binding.recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewProductos.setAdapter(adapter);
        binding.recyclerViewProductos.setHasFixedSize(true);

        adapter.setOnProductoClickListener(new ProductoAdapter.OnProductoClickListener() {
            @Override
            public void onProductoClick(Producto producto) {
                mostrarDetallesProducto(producto);
            }

            @Override
            public void onProductoLongClick(Producto producto) {
                mostrarOpcionesProducto(producto);
            }
        });
    }

    private void observeData() {
        // Observar lista de productos desde Room
        viewModel.getAllProductos().observe(this, productos -> {
            if (productos != null && !productos.isEmpty()) {
                Log.d(TAG, "Productos observados: " + productos.size());
                adapter.setProductos(productos);
                listaProductos = productos;
            } else {
                Log.d(TAG, "No hay productos en Room");
            }
        });

        // Observar total de productos
        viewModel.countProductosActivos().observe(this, count -> {
            if (count != null) {
                binding.tvTotalProductos.setText(String.valueOf(count));
            }
        });

        // Observar valor total del inventario
        viewModel.getValorTotalInventario().observe(this, valor -> {
            if (valor != null) {
                binding.tvValorInventario.setText(
                        String.format(Locale.getDefault(), "$%.2f", valor)
                );
            } else {
                binding.tvValorInventario.setText("$0.00");
            }
        });
    }

    private void setupListeners() {
        // Botón buscar
        binding.btnBuscar.setOnClickListener(v -> {
            String termino = binding.etBuscar.getText().toString().trim();
            if (!termino.isEmpty()) {
                buscarProductos(termino);
            } else {
                Toast.makeText(this, "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón mostrar todos
        binding.btnMostrarTodos.setOnClickListener(v -> {
            binding.etBuscar.setText("");
            observeData();
        });

        // Botón stock bajo
        binding.btnStockBajo.setOnClickListener(v -> mostrarStockBajo());

        // FAB agregar producto
        binding.fabAgregarProducto.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FormProductoActivity.class);
            startActivity(intent);
        });

        // Búsqueda en tiempo real
        binding.etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    buscarProductos(s.toString());
                } else if (s.length() == 0) {
                    adapter.setProductos(listaProductos);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void buscarProductos(String termino) {
        viewModel.searchByName(termino).observe(this, productos -> {
            if (productos != null) {
                adapter.setProductos(productos);
                if (productos.isEmpty()) {
                    Toast.makeText(this, "No se encontraron productos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void mostrarStockBajo() {
        viewModel.getProductosStockBajo().observe(this, productos -> {
            if (productos != null) {
                adapter.setProductos(productos);
                if (productos.isEmpty()) {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("✅ Stock OK")
                            .setMessage("No hay productos con stock bajo")
                            .setPositiveButton("Aceptar", null)
                            .show();
                } else {
                    Toast.makeText(this,
                            "⚠️ " + productos.size() + " productos con stock bajo",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void mostrarDetallesProducto(Producto producto) {
        String mensaje = "Nombre: " + producto.getNombreProducto() + "\n" +
                "Descripción: " + producto.getDescripcion() + "\n" +
                "Precio: $" + String.format(Locale.getDefault(), "%.2f", producto.getPrecioUnitario()) + "\n" +
                "Stock: " + producto.getStockActual() + " (Min: " + producto.getStockMinimo() + ")\n" +
                "Código: " + (producto.getCodigoBarras() != null ? producto.getCodigoBarras() : "N/A");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Detalles del Producto")
                .setMessage(mensaje)
                .setPositiveButton("Cerrar", null)
                .setNeutralButton("Editar", (dialog, which) -> {
                    Intent intent = new Intent(MainActivity.this, FormProductoActivity.class);
                    intent.putExtra("PRODUCTO_ID", producto.getIdProducto());
                    startActivity(intent);
                })
                .show();
    }

    private void mostrarOpcionesProducto(Producto producto) {
        String[] opciones = {"Ver detalles", "Editar", "Actualizar stock", "Eliminar"};

        new MaterialAlertDialogBuilder(this)
                .setTitle(producto.getNombreProducto())
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            mostrarDetallesProducto(producto);
                            break;
                        case 1:
                            Intent intent = new Intent(MainActivity.this, FormProductoActivity.class);
                            intent.putExtra("PRODUCTO_ID", producto.getIdProducto());
                            startActivity(intent);
                            break;
                        case 2:
                            mostrarDialogoActualizarStock(producto);
                            break;
                        case 3:
                            confirmarEliminarProducto(producto);
                            break;
                    }
                })
                .show();
    }

    private void mostrarDialogoActualizarStock(Producto producto) {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nuevo stock");
        input.setText(String.valueOf(producto.getStockActual()));

        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 0, 50, 0);
        input.setLayoutParams(lp);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Actualizar Stock")
                .setMessage("Producto: " + producto.getNombreProducto() + "\nStock actual: " + producto.getStockActual())
                .setView(input)
                .setPositiveButton("Actualizar", (dialog, which) -> {
                    try {
                        int nuevoStock = Integer.parseInt(input.getText().toString());
                        if (nuevoStock >= 0) {
                            viewModel.updateStock(producto.getIdProducto(), nuevoStock);
                            Toast.makeText(this, "✅ Stock actualizado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "❌ El stock no puede ser negativo", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "❌ Valor inválido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarEliminarProducto(Producto producto) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro de eliminar '" + producto.getNombreProducto() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.delete(producto.getIdProducto());
                    Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            cargarProductosFirestore();
            Toast.makeText(this, "Sincronizando...", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void cargarProductosFirestore() {
        binding.progressBar.setVisibility(View.VISIBLE);

        firestoreManager.getProductos(new FirestoreManager.OnProductosListener() {
            @Override
            public void onSuccess(List<Producto> productos) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);

                    if (productos.isEmpty()) {
                        Toast.makeText(MainActivity.this,
                                "No hay productos en Firestore",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    Log.d(TAG, "Productos recibidos de Firestore: " + productos.size());

                    // Guardar productos en Room
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        try {
                            for (Producto producto : productos) {
                                AppDatabase.getDatabase(MainActivity.this)
                                        .productoDao()
                                        .insert(producto);
                            }

                            runOnUiThread(() -> {
                                // Iniciar observación después de la primera sincronización
                                if (primeraSync) {
                                    observeData();
                                    primeraSync = false;
                                }

                                Toast.makeText(MainActivity.this,
                                        "✅ " + productos.size() + " productos sincronizados",
                                        Toast.LENGTH_SHORT).show();
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error al guardar productos en Room: " + e.getMessage());
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this,
                                        "Error al guardar productos: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error al cargar productos: " + error);
                    Toast.makeText(MainActivity.this,
                            "Error: " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Solo observar datos si ya se hizo la primera sincronización
        if (!primeraSync) {
            observeData();
        }
    }
}