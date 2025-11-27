package com.tienda.inventario.ui.adapter;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tienda.inventario.database.AppDatabase;
import com.tienda.inventario.database.entities.Categoria;
import com.tienda.inventario.database.entities.Producto;
import com.tienda.inventario.database.entities.Proveedor;
import com.tienda.inventario.databinding.ActivityFormProductoBinding;
import com.tienda.inventario.viewmodel.ProductoViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FormProductoActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ActivityFormProductoBinding binding;
    private ProductoViewModel viewModel;

    private List<Categoria> categorias;
    private List<Proveedor> proveedores;

    private Categoria categoriaSeleccionada;
    private Proveedor proveedorSeleccionado;

    private int productoId = -1; // -1 = nuevo producto
    private boolean esEdicion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFormProductoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Iniciar Firebase
        db = FirebaseFirestore.getInstance();
        // Cuando el usuario haga clic en guardar
        binding.btnGuardar.setOnClickListener(v -> guardarProducto());

        // Configurar Toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(ProductoViewModel.class);

        // Verificar si es edición
        if (getIntent().hasExtra("PRODUCTO_ID")) {
            productoId = getIntent().getIntExtra("PRODUCTO_ID", -1);
            esEdicion = true;
            binding.toolbar.setTitle("Editar Producto");
        }

        // Cargar datos
        cargarCategoria();
        cargarProveedores();

        // Configurar listeners
        setupListeners();

        // Si es edición, cargar datos del producto
        if (esEdicion) {
            cargarProducto(productoId);
        }
    }

    private void guardarProducto() {
        // Obtener los datos del formulario
        String nombre = binding.etNombre.getText().toString();
        String precioStr = binding.etPrecio.getText().toString();
        String stockStr = binding.etStockMinimo.getText().toString();

        if (nombre.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear un Map con los datos
        Map<String, Object> producto = new HashMap<>();
        producto.put("nombre", nombre);
        producto.put("precio", Double.parseDouble(precioStr));
        producto.put("stock", Integer.parseInt(stockStr));
        producto.put("fecha", System.currentTimeMillis());

        // Guardar en Firestore
        db.collection("productos")
                .add(producto)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Producto guardado exitosamente", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra el formulario y vuelve a MainActivity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarCategoria() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            categorias = AppDatabase.getDatabase(getApplicationContext())
                    .categoriaDao()
                    .getCategoriasSync();

            runOnUiThread(() -> {
                if (categorias != null && !categorias.isEmpty()) {
                    ArrayAdapter<Categoria> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            categorias
                    );
                    binding.spinnerCategoria.setAdapter(adapter);

                    binding.spinnerCategoria.setOnItemClickListener((parent, view, position, id) -> {
                        categoriaSeleccionada = categorias.get(position);
                    });
                } else {
                    mostrarAlertaSinDatos("categorías");
                }
            });
        });
    }

    private void cargarProveedores() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            proveedores = AppDatabase.getDatabase(getApplicationContext())
                    .proveedorDao()
                    .getProveedorSync();

            runOnUiThread(() -> {
                if (proveedores != null && !proveedores.isEmpty()) {
                    ArrayAdapter<Proveedor> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            proveedores
                    );
                    binding.spinnerProveedor.setAdapter(adapter);

                    binding.spinnerProveedor.setOnItemClickListener((parent, view, position, id) -> {
                        proveedorSeleccionado = proveedores.get(position);
                    });
                } else {
                    mostrarAlertaSinDatos("proveedores");
                }
            });
        });
    }

    private void cargarProducto(int id) {
        viewModel.getProductoById(id).observe(this, producto -> {
            if (producto != null) {
                binding.etNombre.setText(producto.getNombreProducto());
                binding.etDescripcion.setText(producto.getDescripcion());
                binding.etPrecio.setText(String.valueOf(producto.getPrecioUnitario()));
                binding.etStockActual.setText(String.valueOf(producto.getStockActual()));
                binding.etStockMinimo.setText(String.valueOf(producto.getStockMinimo()));
                binding.etCodigoBarras.setText(producto.getCodigoBarras());

                // Seleccionar categoría y proveedor
                seleccionarCategoriaYProveedor(producto);
            }
        });
    }

    private void seleccionarCategoriaYProveedor(Producto producto) {
        // Esperar a que se carguen las listas
        new Thread(() -> {
            try {
                Thread.sleep(500); // Dar tiempo a que se carguen
                runOnUiThread(() -> {
                    // Buscar y seleccionar categoría
                    for (int i = 0; i < categorias.size(); i++) {
                        if (categorias.get(i).getIdCategoria() == producto.getIdCategoria()) {
                            binding.spinnerCategoria.setText(categorias.get(i).getNombreCategoria(), false);
                            categoriaSeleccionada = categorias.get(i);
                            break;
                        }
                    }

                    // Buscar y seleccionar proveedor
                    for (int i = 0; i < proveedores.size(); i++) {
                        if (proveedores.get(i).getIdProveedor() == producto.getIdProveedor()) {
                            binding.spinnerProveedor.setText(proveedores.get(i).getNombreProveedor(), false);
                            proveedorSeleccionado = proveedores.get(i);
                            break;
                        }
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupListeners() {
        // Botón guardar
        binding.btnGuardar.setOnClickListener(v -> guardarProducto());

        // Botón cancelar
        binding.btnCancelar.setOnClickListener(v -> {
            if (hayDatosIngresados()) {
                confirmarCancelacion();
            } else {
                finish();
            }
        });
    }

    private void guardarProducto() {
        // Validar campos
        if (!validarCampos()) {
            return;
        }

        try {
            // Crear o actualizar producto
            Producto producto;

            if (esEdicion) {
                producto = new Producto();
                producto.setIdProducto(productoId);
            } else {
                producto = new Producto();
            }

            producto.setNombreProducto(binding.etNombre.getText().toString().trim());
            producto.setDescripcion(binding.etDescripcion.getText().toString().trim());
            producto.setPrecioUnitario(Double.parseDouble(binding.etPrecio.getText().toString().trim()));
            producto.setStockActual(Integer.parseInt(binding.etStockActual.getText().toString().trim()));
            producto.setStockMinimo(Integer.parseInt(binding.etStockMinimo.getText().toString().trim()));
            producto.setIdCategoria(categoriaSeleccionada.getIdCategoria());
            producto.setIdProveedor(proveedorSeleccionado.getIdProveedor());
            producto.setCodigoBarras(binding.etCodigoBarras.getText().toString().trim());
            producto.setActivo(true);

            // Guardar en base de datos
            if (esEdicion) {
                viewModel.update(producto);
                Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.insert(producto);
                Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show();
            }

            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Error en formato numérico", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validarCampos() {
        // Nombre
        if (binding.etNombre.getText().toString().trim().isEmpty()) {
            binding.etNombre.setError("Campo requerido");
            binding.etNombre.requestFocus();
            return false;
        }

        // Precio
        if (binding.etPrecio.getText().toString().trim().isEmpty()) {
            binding.etPrecio.setError("Campo requerido");
            binding.etPrecio.requestFocus();
            return false;
        }

        // Stock actual
        if (binding.etStockActual.getText().toString().trim().isEmpty()) {
            binding.etStockActual.setError("Campo requerido");
            binding.etStockActual.requestFocus();
            return false;
        }

        // Stock mínimo
        if (binding.etStockMinimo.getText().toString().trim().isEmpty()) {
            binding.etStockMinimo.setError("Campo requerido");
            binding.etStockMinimo.requestFocus();
            return false;
        }

        // Categoría
        if (categoriaSeleccionada == null) {
            Toast.makeText(this, "Seleccione una categoría", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Proveedor
        if (proveedorSeleccionado == null) {
            Toast.makeText(this, "Seleccione un proveedor", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean hayDatosIngresados() {
        return !binding.etNombre.getText().toString().trim().isEmpty() ||
                !binding.etDescripcion.getText().toString().trim().isEmpty() ||
                !binding.etPrecio.getText().toString().trim().isEmpty() ||
                !binding.etStockActual.getText().toString().trim().isEmpty() ||
                !binding.etStockMinimo.getText().toString().trim().isEmpty();
    }

    private void confirmarCancelacion() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancelar")
                .setMessage("¿Desea descartar los cambios?")
                .setPositiveButton("Sí, descartar", (dialog, which) -> finish())
                .setNegativeButton("Continuar editando", null)
                .show();
    }

    private void mostrarAlertaSinDatos(String tipo) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sin " + tipo)
                .setMessage("No hay " + tipo + " registradas. Por favor, agregue " + tipo + " primero.")
                .setPositiveButton("Aceptar", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (hayDatosIngresados()) {
            confirmarCancelacion();
        } else {
            super.onBackPressed();
        }
    }
    // En tu Activity


}