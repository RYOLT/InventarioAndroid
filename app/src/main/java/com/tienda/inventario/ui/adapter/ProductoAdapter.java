package com.tienda.inventario.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.tienda.inventario.R;
import com.tienda.inventario.database.entities.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Producto> productos = new ArrayList<>();
    private OnProductoClickListener listener;

    // Interface para clicks
    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
        void onProductoLongClick(Producto producto);
    }

    public void setOnProductoClickListener(OnProductoClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productos.get(position);
        holder.bind(producto);
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
        notifyDataSetChanged();
    }

    public Producto getProductoAt(int position) {
        return productos.get(position);
    }

    class ProductoViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardProducto;
        private TextView tvNombreProducto;
        private TextView tvPrecio;
        private TextView tvDescripcion;
        private TextView tvCategoria;
        private TextView tvProveedor;
        private TextView tvStock;
        private TextView tvStockMinimo;
        private TextView tvAlertaStock;
        private TextView tvCodigoBarras;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            cardProducto = itemView.findViewById(R.id.cardProducto);
            tvNombreProducto = itemView.findViewById(R.id.tvNombreProducto);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvProveedor = itemView.findViewById(R.id.tvProveedor);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvStockMinimo = itemView.findViewById(R.id.tvStockMinimo);
            tvAlertaStock = itemView.findViewById(R.id.tvAlertaStock);
            tvCodigoBarras = itemView.findViewById(R.id.tvCodigoBarras);
        }

        public void bind(final Producto producto) {
            tvNombreProducto.setText(producto.getNombreProducto());
            tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecioUnitario()));
            tvDescripcion.setText(producto.getDescripcion());
            tvStock.setText(String.valueOf(producto.getStockActual()));
            tvStockMinimo.setText(String.format("(Min: %d)", producto.getStockMinimo()));

            // Para categoría y proveedor, mostramos los IDs
            tvCategoria.setText("Categoría: " + producto.getIdCategoria());
            tvProveedor.setText("Proveedor: " + producto.getIdProveedor());

            // Mostrar código de barras si existe
            if (producto.getCodigoBarras() != null && !producto.getCodigoBarras().isEmpty()) {
                tvCodigoBarras.setText("Código: " + producto.getCodigoBarras());
                tvCodigoBarras.setVisibility(View.VISIBLE);
            } else {
                tvCodigoBarras.setVisibility(View.GONE);
            }

            // Alerta de stock bajo
            if (producto.isBajoStock()) {
                tvAlertaStock.setVisibility(View.VISIBLE);
                tvStock.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
            } else {
                tvAlertaStock.setVisibility(View.GONE);
                tvStock.setTextColor(itemView.getContext().getColor(android.R.color.black));
            }

            // Click listeners
            cardProducto.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductoClick(producto);
                }
            });

            cardProducto.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onProductoLongClick(producto);
                }
                return true;
            });
        }
    }
}