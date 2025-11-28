package com.tienda.inventario.ui.adapter;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tienda.inventario.database.AppDatabase;
import com.tienda.inventario.database.FirestoreManager;
import com.tienda.inventario.database.entities.Categoria;
import com.tienda.inventario.database.entities.Producto;
import com.tienda.inventario.database.entities.Proveedor;
import com.tienda.inventario.databinding.ActivityFormProductoBinding;

import java.util.ArrayList;
import java.util.List;

public class FormProductoActivity extends AppCompatActivity {

    private ActivityFormProductoBinding binding;
    private FirestoreManager firestoreManager;
    private List<Categoria> listaCategorias = new ArrayList<>();
    private List<Proveedor> listaProveedores = new ArrayList<>();
    private int categoriaSeleccionadaId = -1;
    private int proveedorSeleccionadoId = -1;
    private int productoId = -1;
    private boolean esEdicion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFormProductoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestoreManager = FirestoreManager.getInstance();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent().hasExtra("PRODUCTO_ID")) {
            productoId = getIntent().getIntExtra("PRODUCTO_ID", -1);
            esEdicion = true;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Editar Producto");
            }
        }

        cargarCategorias();
        cargarProveedores();
        setupListeners();

        if (esEdicion && productoId != -1) {
            cargarProducto(productoId);
        }
    }

    private void setupListeners() {
        binding.btnGuardar.setOnClickListener(v -> guardarProducto());
        binding.btnCancelar.setOnClickListener(v -> finish());
    }

    private void cargarCategorias() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            listaCategorias = AppDatabase.getDatabase(this).categoriaDao().getAllCategoriasList();

            runOnUiThread(() -> {
                if (listaCategorias.isEmpty()) {
                    firestoreManager.getCategorias(new FirestoreManager.OnCategoriasListener() {
                        @Override
                        public void onSuccess(List<Categoria> categorias) {
                            listaCategorias = categorias;
                            configurarSpinnerCategorias();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(FormProductoActivity.this,
                                    "Error al cargar categorías: " + error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    configurarSpinnerCategorias();
                }
            });
        });
    }

    private void configurarSpinnerCategorias() {
        List<String> nombresCategoria = new ArrayList<>();
        for (Categoria cat : listaCategorias) {
            nombresCategoria.add(cat.getNombreCategoria());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                nombresCategoria);

        binding.spinnerCategoria.setAdapter(adapter);
        binding.spinnerCategoria.setOnItemClickListener((parent, view, position, id) -> {
            categoriaSeleccionadaId = listaCategorias.get(position).getIdCategoria();
        });
    }

    private void cargarProveedores() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            listaProveedores = AppDatabase.getDatabase(this).proveedorDao().getAllProveedoresList();

            runOnUiThread(() -> {
                if (listaProveedores.isEmpty()) {
                    firestoreManager.getProveedores(new FirestoreManager.OnProveedoresListener() {
                        @Override
                        public void onSuccess(List<Proveedor> proveedores) {
                            listaProveedores = proveedores;
                            configurarSpinnerProveedores();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(FormProductoActivity.this,
                                    "Error al cargar proveedores: " + error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    configurarSpinnerProveedores();
                }
            });
        });
    }

    private void configurarSpinnerProveedores() {
        List<String> nombresProveedor = new ArrayList<>();
        for (Proveedor prov : listaProveedores) {
            nombresProveedor.add(prov.getNombreProveedor());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                nombresProveedor);

        binding.spinnerProveedor.setAdapter(adapter);
        binding.spinnerProveedor.setOnItemClickListener((parent, view, position, id) -> {
            proveedorSeleccionadoId = listaProveedores.get(position).getIdProveedor();
        });
    }

    private void cargarProducto(int id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Producto producto = AppDatabase.getDatabase(this)
                    .productoDao()
                    .getProductoById(id)
                    .getValue();

            if (producto != null) {
                runOnUiThread(() -> {
                    binding.etNombre.setText(producto.getNombreProducto());
                    binding.etDescripcion.setText(producto.getDescripcion());
                    binding.etPrecio.setText(String.valueOf(producto.getPrecioUnitario()));
                    binding.etStockActual.setText(String.valueOf(producto.getStockActual()));
                    binding.etStockMinimo.setText(String.valueOf(producto.getStockMinimo()));
                    binding.etCodigoBarras.setText(producto.getCodigoBarras());

                    for (int i = 0; i < listaCategorias.size(); i++) {
                        if (listaCategorias.get(i).getIdCategoria() == producto.getIdCategoria()) {
                            binding.spinnerCategoria.setText(listaCategorias.get(i).getNombreCategoria(), false);
                            categoriaSeleccionadaId = producto.getIdCategoria();
                            break;
                        }
                    }

                    for (int i = 0; i < listaProveedores.size(); i++) {
                        if (listaProveedores.get(i).getIdProveedor() == producto.getIdProveedor()) {
                            binding.spinnerProveedor.setText(listaProveedores.get(i).getNombreProveedor(), false);
                            proveedorSeleccionadoId = producto.getIdProveedor();
                            break;
                        }
                    }
                });
            }
        });
    }

    private void guardarProducto() {
        if (!validarCampos()) {
            return;
        }

        Producto producto = new Producto();
        producto.setNombreProducto(binding.etNombre.getText().toString().trim());
        producto.setDescripcion(binding.etDescripcion.getText().toString().trim());
        producto.setPrecioUnitario(Double.parseDouble(binding.etPrecio.getText().toString().trim()));
        producto.setStockActual(Integer.parseInt(binding.etStockActual.getText().toString().trim()));
        producto.setStockMinimo(Integer.parseInt(binding.etStockMinimo.getText().toString().trim()));
        producto.setCodigoBarras(binding.etCodigoBarras.getText().toString().trim());
        producto.setIdCategoria(categoriaSeleccionadaId);
        producto.setIdProveedor(proveedorSeleccionadoId);

        firestoreManager.agregarProducto(producto, new FirestoreManager.OnSuccessListener() {
            @Override
            public void onSuccess() {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    if (esEdicion) {
                        producto.setIdProducto(productoId);
                        AppDatabase.getDatabase(FormProductoActivity.this)
                                .productoDao()
                                .update(producto);
                    } else {
                        AppDatabase.getDatabase(FormProductoActivity.this)
                                .productoDao()
                                .insert(producto);
                    }
                });

                runOnUiThread(() -> {
                    Toast.makeText(FormProductoActivity.this,
                            esEdicion ? "Producto actualizado" : "Producto guardado",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                Toast.makeText(FormProductoActivity.this,
                        "Error al guardar: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validarCampos() {
        if (TextUtils.isEmpty(binding.etNombre.getText())) {
            binding.etNombre.setError("Campo obligatorio");
            binding.etNombre.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(binding.etPrecio.getText())) {
            binding.etPrecio.setError("Campo obligatorio");
            binding.etPrecio.requestFocus();
            return false;
        }

        try {
            double precio = Double.parseDouble(binding.etPrecio.getText().toString().trim());
            if (precio <= 0) {
                binding.etPrecio.setError("El precio debe ser mayor a 0");
                binding.etPrecio.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            binding.etPrecio.setError("Precio inválido");
            binding.etPrecio.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(binding.etStockActual.getText())) {
            binding.etStockActual.setError("Campo obligatorio");
            binding.etStockActual.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(binding.etStockMinimo.getText())) {
            binding.etStockMinimo.setError("Campo obligatorio");
            binding.etStockMinimo.requestFocus();
            return false;
        }

        if (categoriaSeleccionadaId == -1) {
            Toast.makeText(this, "Seleccione una categoría", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (proveedorSeleccionadoId == -1) {
            Toast.makeText(this, "Seleccione un proveedor", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}