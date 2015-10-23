package com.example.ddoggett.androidpaydemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

 import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentMethodTokenizationType;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.BuyButtonAppearance;
import com.google.android.gms.wallet.fragment.BuyButtonText;
import com.google.android.gms.wallet.fragment.Dimension;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;
import java.util.ArrayList;
import java.util.List;

public class CheckoutScreen extends Fragment implements View.OnClickListener {

    public static final String EXTRA_SHOPPING_CART = "EXTRA_SHOPPING_CART";
    private SupportWalletFragment mWalletFragment;
    private PaymentMethodTokenizationParameters mPaymentMethodParameters;
    public TextView shippingText;
    public TextView taxText;
    public TextView totalText;
    public Button continueWithOrder;
    public Button continueWShopping;
    public FrameLayout continueWithAndroidPay;
    public ArrayList<Product> shoppingCart;
    public  MaskedWalletRequest maskedWalletRequest;
    public static Fragment newInstance(ArrayList<Product> cart) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_SHOPPING_CART, cart);
        Fragment checkoutScreen = new CheckoutScreen();
        checkoutScreen.setArguments(bundle);
        return checkoutScreen;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shoppingCart = ((HomeActivity)getActivity()).getShoppingCart();
        // in order to receive the correct token from Google Services
        // set the payment method parameters for Stripe
        mPaymentMethodParameters = PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(PaymentMethodTokenizationType.PAYMENT_GATEWAY)
                .addParameter("gateway", "stripe")
                .addParameter("stripe:publishableKey", ((HomeActivity) getActivity()).STRIPE_PUBLISHABLE_KEY)
                        .addParameter("stripe:version", com.stripe.Stripe.VERSION)
                        .build();
        // initialize WalletFragment
        createAndAddWalletFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.checkout_screen, null, false);
        shippingText = (TextView) view.findViewById(R.id.shippingText);
        shippingText.setText("4.50");
        taxText = (TextView) view.findViewById(R.id.taxText);
        taxText.setText("1.50");
        totalText = (TextView) view.findViewById(R.id.totalText);
        totalText.setText(maskedWalletRequest.getEstimatedTotalPrice());
        continueWithOrder = (Button) view.findViewById(R.id.continueWithOrderButton);
        continueWShopping = (Button) view.findViewById(R.id.continueWShoppingButton);
        continueWithAndroidPay = (FrameLayout) view.findViewById(R.id.continueWithAndroidPayButton);
        continueWithOrder.setOnClickListener(this);
        continueWShopping.setOnClickListener(this);
        continueWithAndroidPay.setOnClickListener(this);
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

    private void createAndAddWalletFragment() {
        // set attributes to customize the look and feel of WalletFragment
        WalletFragmentStyle walletFragmentStyle = new WalletFragmentStyle()
                .setBuyButtonText(BuyButtonText.BUY_WITH_GOOGLE)
                .setBuyButtonAppearance(BuyButtonAppearance.CLASSIC)
                .setBuyButtonWidth(Dimension.MATCH_PARENT);
        // class that handles WalletFragment configuration
        // on the checkout screen set Mode to BUY_BUTTON
        WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                .setEnvironment(WalletConstants.ENVIRONMENT_SANDBOX) // Environment to use when creating an instance of Wallet.WalletOptions
                .setFragmentStyle(walletFragmentStyle)
                .setTheme(WalletConstants.THEME_LIGHT)
                .setMode(WalletFragmentMode.BUY_BUTTON)
                .build();
        mWalletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);

        // create MaskedWalletRequest
        maskedWalletRequest = createStripeMaskedWalletRequest();


        WalletFragmentInitParams.Builder startParamsBuilder = WalletFragmentInitParams.newBuilder()
                // pass in maskedWalletRequest so WalletFragment can launch Andriod Pay upon user click
                .setMaskedWalletRequest(maskedWalletRequest)
                // MaskedWallet will be passed back to onActivityResult by Android Pay once the user
                // selects their card
                .setMaskedWalletRequestCode(HomeActivity.REQUEST_CODE_MASKED_WALLET);
        mWalletFragment.initialize(startParamsBuilder.build());

        // add Wallet fragment to the UI
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.continueWithAndroidPayButton, mWalletFragment)
                .commit();
    }


    private MaskedWalletRequest createStripeMaskedWalletRequest() {
        // Build a List of all line items
        List<LineItem> lineItems = ((HomeActivity)getActivity()).buildLineItems();

        // Calculate the cart total by iterating over the line items.
        String cartTotal = ((HomeActivity)getActivity()).calculateCartTotal(lineItems);

        MaskedWalletRequest.Builder builder = MaskedWalletRequest.newBuilder()
                .setMerchantName("Merchant Name")
                .setPhoneNumberRequired(true)
                .setShippingAddressRequired(true)
                .setCurrencyCode("USD")
                .setEstimatedTotalPrice(cartTotal)
                .setCart(Cart.newBuilder()
                        .setCurrencyCode("USD")
                        .setTotalPrice(cartTotal)
                        .setLineItems(lineItems)
                        .build());

            builder.setPaymentMethodTokenizationParameters(mPaymentMethodParameters);

        return builder.build();
    }


    @Override
    public void onClick(View view) {
        if(view == continueWithAndroidPay) {
            Toast.makeText(getActivity(), "Pay with android", Toast.LENGTH_SHORT).show();

        } else if(view == continueWithOrder) {
            Toast.makeText(getActivity(), "Continue with order", Toast.LENGTH_SHORT).show();

        } else if(view == continueWShopping) {
            Toast.makeText(getActivity(), "Go back to products", Toast.LENGTH_SHORT).show();
        }
    }
}
