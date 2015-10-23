package com.example.ddoggett.androidpaydemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class ProductRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private ArrayList<Product> productList;
    private OnProductSelectedListener productsListener;

    public ProductRecyclerAdapter(Context context, OnProductSelectedListener listener){
        this.context = context;
        this.productsListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(LayoutInflater.from(context).inflate(R.layout.product_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Product product = productList.get(position);
        ((ProductViewHolder)holder).name.setText(product.name);
        ((ProductViewHolder)holder).price.setText(String.valueOf(product.price));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                productsListener.onProductSelected(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void setProductList(ArrayList<Product> list) {
        list.toString();
        productList = list;
        notifyDataSetChanged();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView price;
        TextView name;
        ImageView image;
        public ProductViewHolder(View parent){
            super(parent);
            price = (TextView) parent.findViewById(R.id.productPrice);
            name = (TextView) parent.findViewById(R.id.productName);
        }

    }

}
