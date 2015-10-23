package com.example.ddoggett.androidpaydemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;
import java.util.List;


public class ReviewScreen extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private SupportWalletFragment mWalletFragment;
    private MaskedWallet maskedWallet;
    protected GoogleApiClient mGoogleApiClient;
    private Button confirmPurchaseButton;
    private FullWalletRequest fullWalletRequest;
    private ConnectionResult connectionResult;

    public static Fragment newInstance(MaskedWallet maskedWallet) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(HomeActivity.EXTRA_MASKED_WALLET, maskedWallet);
        Fragment reviewScreen = new ReviewScreen();
        reviewScreen.setArguments(bundle);
        return reviewScreen;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);maskedWallet = getArguments().getParcelable(HomeActivity.EXTRA_MASKED_WALLET);
        // initialize WalletFragment using MaskedWallet information
        createAndAddWalletFragment();
        // create FullWalletRequest
        fullWalletRequest = createFullWalletRequest(maskedWallet.getGoogleTransactionId());

        // Set up an API client
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_SANDBOX)
                        .setTheme(WalletConstants.THEME_HOLO_LIGHT)
                        .build())
                .build();

    }

    @Override
    public void onStart() {
        super.onStart();
        // Connect to Google Play Services
        mGoogleApiClient.connect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.review_screen, null, false);
        confirmPurchaseButton = (Button)view.findViewById(R.id.makePurchase);
        confirmPurchaseButton.setOnClickListener(this);
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

    private void  createAndAddWalletFragment() {
        // set attributes to customize the look and feel of WalletFragment
        WalletFragmentStyle walletFragmentStyle = new WalletFragmentStyle()
                .setMaskedWalletDetailsTextAppearance(
                        R.style.WalletFragmentText)
                .setMaskedWalletDetailsHeaderTextAppearance(
                        R.style.SelectionDetailsHeaderText)
                .setMaskedWalletDetailsBackgroundColor(
                        getResources().getColor(R.color.white));
        // class that handles WalletFragment configuration
        // on the confirmation screen set Mode to SELECTION_DETAILS
        WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                .setEnvironment(WalletConstants.ENVIRONMENT_SANDBOX)
                .setFragmentStyle(walletFragmentStyle)
                .setTheme(WalletConstants.THEME_LIGHT)
                .setMode(WalletFragmentMode.SELECTION_DETAILS)
                .build();
        mWalletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);

        WalletFragmentInitParams.Builder startParamsBuilder = WalletFragmentInitParams.newBuilder()
                // pass in maskedWallet so WalletFragment can display the customer's payment details
                .setMaskedWallet(maskedWallet)
                        // set request code if user decides to change their payment information
                        // If the customer decides to change the shipping or billing information
                        // they selected on the previous screen, by clicking either of the Change
                        // buttons that WalletFragment displays, Android Pay will be relaunched
                .setMaskedWalletRequestCode(HomeActivity.REQUEST_CODE_CHANGE_MASKED_WALLET);
        mWalletFragment.initialize(startParamsBuilder.build());

        // add Wallet fragment to the UI
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.dynamic_wallet_masked_wallet_fragment, mWalletFragment)
                .commit();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // don't need to implement this
    }

    @Override
    public void onConnectionSuspended(int i) {
        // don't need to implement this
    }

    @Override
    public void onClick(View view) {
        if(view == confirmPurchaseButton) {
            // The customer wants to go through with transaction
            // so fire off FullWalletRequest
            Wallet.Payments.loadFullWallet(mGoogleApiClient, fullWalletRequest,
                    HomeActivity.REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET);
        }
    }

    public FullWalletRequest createFullWalletRequest(String googleTransactionId) {

        List<LineItem> lineItems =  ((HomeActivity)getActivity()).buildLineItems();

        String cartTotal = ((HomeActivity)getActivity()).calculateCartTotal(lineItems);

        return FullWalletRequest.newBuilder()
                .setGoogleTransactionId(googleTransactionId)
                .setCart(Cart.newBuilder()
                        .setCurrencyCode("USD")
                        .setTotalPrice(cartTotal)
                        .setLineItems(lineItems)
                        .build())
                .build();
    }

    @Override
    public void onStop() {
        super.onStop();

        // Disconnect from Google Play Services
        mGoogleApiClient.disconnect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        connectionResult = result;
        ((HomeActivity)getActivity()).dismissProgressDialog();
    }
}
