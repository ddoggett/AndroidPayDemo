package com.example.ddoggett.androidpaydemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.WalletConstants;
import com.stripe.android.compat.AsyncTask;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomeActivity extends FragmentActivity {
    private static final String TAG = "HomeActivity";
    public static final int REQUEST_CODE_CHANGE_MASKED_WALLET = 1002;
    public static final int REQUEST_CODE_MASKED_WALLET = 1001;
    public static final int REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET = 1004;
    public static final String EXTRA_MASKED_WALLET = "EXTRA_MASKED_WALLET";
    public static final String EXTRA_SHOPPING_CART = "EXTRA_SHOPPING_CART";
    public static String STRIPE_SECRET_KEY = "STRIPE_SECRET_KEY";
    public static String STRIPE_PUBLISHABLE_KEY = "STRIPE_PUBLISHABLE_KEY";
    public ArrayList<Product> shoppingCart = new ArrayList<>();
    protected ProgressDialog mProgressDialog;

    @Override
    protected void onStop() {
        super.onStop();

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(savedInstanceState==null) {
            Fragment fragment = new ProductListFragment();
            show(fragment);
        } else {
            shoppingCart = savedInstanceState.getParcelableArrayList(EXTRA_SHOPPING_CART);
        }
        initializeProgressDialog();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(EXTRA_SHOPPING_CART, shoppingCart);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mProgressDialog.hide();
        // retrieve the error code, if available
        int errorCode = -1;
        if (data != null) {
            errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
        }
        switch (requestCode) {
            case REQUEST_CODE_MASKED_WALLET:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // Android Pay has successfully returned the customer's MaskedWallet
                        MaskedWallet maskedWallet =
                                data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                        // Everything needed to render the confirmation screen has been acquired,
                        // so launch the confirmation screen passing in the MaskedWallet
                        goToReviewScreen(maskedWallet);
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                        // handle error
                        break;
                }
                break;
            case REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data.hasExtra(WalletConstants.EXTRA_FULL_WALLET)) {
                            // Google Services have successfully returned the customer's FullWallet
                            FullWallet fullWallet =
                                    data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
                            // Everything needed to process the customer's payment has been acquired
                            sendToken(fullWallet);
                        } else if (data.hasExtra(WalletConstants.EXTRA_MASKED_WALLET)) {
                            // relaunch confirmation scren with new masked wallet
                            MaskedWallet maskedWallet =
                                    data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                            goToReviewScreen(maskedWallet);
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                        // handle error
                        break;
                }
                break;
            case WalletConstants.RESULT_ERROR:
                // handle error
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

        @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    public void show(Fragment fragment) {
        FragmentTransaction lft = getSupportFragmentManager().beginTransaction();
        lft.replace(R.id.fragmentContainer, fragment).addToBackStack(null).commit();
    }

    public void addToCart(Product p) {
        shoppingCart.add(p);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    public void goToCheckout() {
        show(CheckoutScreen.newInstance(shoppingCart));
    }

    private void goToReviewScreen(MaskedWallet maskedWallet) {
        show(ReviewScreen.newInstance(maskedWallet));
    }

    public void showProductDetail(Product p) {
        show(ProductDetailScreen.newInstance(p));
    }

    public ArrayList<Product> getShoppingCart() {
        return shoppingCart;
    }


    private void sendToken(FullWallet fullWallet) {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        final FullWallet myWallet = fullWallet;
        // extract the payment token from fullWallet
        String tokenJSON = myWallet.getPaymentMethodToken().getToken();
        com.stripe.model.Token stripeToken = com.stripe.model.Token.GSON.fromJson(tokenJSON, com.stripe.model.Token.class);
        Log.d(TAG, "PaymentMethodToken:" + stripeToken.getId());
        // send request on separate thread to prevent NetworkOnMainThreadException
        new PostToken().execute(stripeToken.getId());
    }

    public void dismissProgressDialog() {
        mProgressDialog.dismiss();
    }


    private class PostToken extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Using Stripe API, create a Charge object that will handle sending the request
            String token = params[0];
            RequestOptions requestOptions = (new RequestOptions.RequestOptionsBuilder()).setApiKey(HomeActivity.STRIPE_SECRET_KEY).build();
            Map<String, Object> chargeMap = new HashMap<String, Object>();
            chargeMap.put("amount", 100);
            chargeMap.put("currency", "usd");
            chargeMap.put("source", token);
            try {
                Charge charge = Charge.create(chargeMap, requestOptions);
                System.out.println("Charge: " + charge);
            } catch (StripeException e) {
                e.printStackTrace();
            }
            return null;
        }
    }



    /**
     * Build a list of line items based on the Products contained in shoppingCart
     * and totaling the shipping and tax
     */
    public List<LineItem> buildLineItems() {
        List<LineItem> list = new ArrayList<LineItem>();
        int totalShipping = 0;
        int totalTax = 0;

        // create a line item for each item in shopping cart
        for(Product p : shoppingCart) {
            list.add(LineItem.newBuilder()
                    .setCurrencyCode("USD")
                    .setDescription(p.description)
                    .setQuantity("1")
                    .setUnitPrice(p.price + "")
                    .setTotalPrice(p.price + "")
                    .build());
            // total the shipping
            totalShipping+=p.shipping;
            // total the tax
            totalTax+=p.tax;
        }

        // add shipping charge
        list.add(LineItem.newBuilder()
                .setCurrencyCode("USD")
                .setDescription("Shipping")
                .setRole(LineItem.Role.SHIPPING)
                .setTotalPrice(String.valueOf(totalShipping))
                .build());

        // add tax charge
        list.add(LineItem.newBuilder()
                .setCurrencyCode("USD")
                .setDescription("Tax")
                .setRole(LineItem.Role.TAX)
                .setTotalPrice(String.valueOf(totalTax))
                .build());
        return list;
    }

    /**
     *  Calculates shopping cart total by iterating over each LineItem in lineItems
     */
    public static String calculateCartTotal(List<LineItem> lineItems) {
        BigDecimal cartTotal = BigDecimal.ZERO;

        for (LineItem lineItem: lineItems) {
            BigDecimal lineItemTotal = lineItem.getTotalPrice() == null ?
                    new BigDecimal(lineItem.getUnitPrice())
                            .multiply(new BigDecimal(lineItem.getQuantity())) :
                    new BigDecimal(lineItem.getTotalPrice());

            cartTotal = cartTotal.add(lineItemTotal);
        }

        return cartTotal.setScale(2, RoundingMode.HALF_EVEN).toString();
    }

    protected void initializeProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
              //  mHandleFullWalletWhenReady = false;
            }
        });
    }
}
