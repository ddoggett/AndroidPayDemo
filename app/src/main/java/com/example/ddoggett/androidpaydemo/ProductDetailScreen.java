package com.example.ddoggett.androidpaydemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class ProductDetailScreen extends Fragment implements View.OnClickListener{
    private static final String TAG = "ProductDetailScreen";
    public static final String EXTRA_PRODUCT = "EXTRA_PRODUCT";
    public TextView productName;
    public TextView productPrice;
    public Button addToCartButton;
    public Product product;

    public static Fragment newInstance(Product p) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_PRODUCT, p);
        Fragment productDetailScreen = new ProductDetailScreen();
        productDetailScreen.setArguments(bundle);
        return productDetailScreen;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState==null) {
            product = getArguments().getParcelable(EXTRA_PRODUCT);
        } else {
            product = savedInstanceState.getParcelable(EXTRA_PRODUCT);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.product_detail_screen, null, false);
        productName = (TextView) view.findViewById(R.id.productName);
        productPrice = (TextView) view.findViewById(R.id.productPrice);
        addToCartButton = (Button) view.findViewById(R.id.button);
        productName.setText(product.name);
        productPrice.setText("$" + String.valueOf(product.price));
        addToCartButton.setOnClickListener(this);
        return view;
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_PRODUCT, product);
    }

    @Override
    public void onClick(View view) {
        if(view == addToCartButton) {
            ((HomeActivity)getActivity()).addToCart(product);
            ((HomeActivity)getActivity()).goToCheckout();
        }
    }
}
