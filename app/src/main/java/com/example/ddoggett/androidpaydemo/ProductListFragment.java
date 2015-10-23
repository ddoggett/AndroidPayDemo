package com.example.ddoggett.androidpaydemo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ProductListFragment extends Fragment implements OnProductSelectedListener {

    public RecyclerView productRecyclerView;
    public ProductRecyclerAdapter productRecyclerAdapter;
    public LinearLayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        productRecyclerAdapter = new ProductRecyclerAdapter(getActivity(),this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.product_list_layout, null, false);
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        productRecyclerView = (RecyclerView) view.findViewById(R.id.productRecyclerView);
        productRecyclerView.setLayoutManager(layoutManager);
        productRecyclerAdapter.setProductList(makeProducts(getActivity()));
        productRecyclerView.setAdapter(productRecyclerAdapter);
        return view;
    }

    public static ArrayList<Product> makeProducts(Context context) {
        int[] prices = context.getResources().getIntArray(R.array.product_prices);
        String[] names = context.getResources().getStringArray(R.array.product_names);
        ArrayList<Product> products = new ArrayList<>();

        int index = 0;
        for(int i : prices) {
            products.add(new Product(prices[index], names[index]));
            index++;
        }
        return products;
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onProductSelected(Product p) {
        ((HomeActivity)getActivity()).showProductDetail(p);
    }
}
