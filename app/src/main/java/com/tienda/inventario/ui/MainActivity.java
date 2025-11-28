package com.tienda.inventario.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tienda.inventario.R;
import com.tienda.inventario.database.entities.Producto;
import com.tienda.inventario.databinding.ActivityMainBinding;
import com.tienda.inventario.database.FirestoreManager;
import com.tienda.inventario.ui.adapter.FormProductoActivity;
import com.tienda.inventario.ui.adapter.ProductoAdapter;
import com.tienda.inventario.viewmodel.ProductoViewModel;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirestoreManager firestoreManager;
    private ActivityMainBinding binding;
    private ProductoViewModel viewModel;
    private ProductoAdapter adapter;
    private List<Producto> listaProductos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Firestore Manager
        firestoreManager = FirestoreManager.getInstance();

        // Configurar Toolbar
        setSupportActionBar(binding.toolbar);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(ProductoViewModel.class);

        // Configurar RecyclerView
        setupRecyclerView();

        // Observar datos desde Room (base de datos local)
        observeData();

        // Configurar listeners
        setupListeners();

        // Cargar productos desde Firestore
        cargarProductosFirestore();
    }

    private void setupRecyclerView() {
        adapter = new ProductoAdapter();
        binding.recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewProductos.setAdapter(adapter);
        binding.recyclerViewProductos.setHasFixedSize(true);

        // Configurar clicks en productos
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
            if (productos != null) {
                adapter.setProductos(productos);
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
            viewModel.getAllProductos().observe(this, productos -> {
                if (productos != null) {
                    adapter.setProductos(productos);
                }
            });
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
                    viewModel.getAllProductos().observe(MainActivity.this, productos -> {
                        if (productos != null) {
                            adapter.setProductos(productos);
                        }
                    });
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
            viewModel.getAllProductos().observe(this, productos -> {
                if (productos != null) {
                    adapter.setProductos(productos);
                }
            });
            cargarProductosFirestore();
            Toast.makeText(this, "Lista actualizada", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void cargarProductosFirestore() {
        binding.progressBar.setVisibility(View.VISIBLE);

        firestoreManager.getProductos(new FirestoreManager.OnProductosListener() {
            @Override
            public void onSuccess(List<Producto> productos) {
                binding.progressBar.setVisibility(View.GONE);

                // Guardar productos de Firestore en Room
                for (Producto producto : productos) {
                    viewModel.insert(producto);
                }

                Toast.makeText(MainActivity.this,
                        "Sincronizado: " + productos.size() + " productos",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar productos cuando volvemos a la actividad
        viewModel.getAllProductos().observe(this, productos -> {
            if (productos != null) {
                adapter.setProductos(productos);
            }
        });
    }
}