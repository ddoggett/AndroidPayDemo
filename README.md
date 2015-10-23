# AndroidPayDemo
This example app demostrates the core components of the Android Pay API. It also uses the Stripe API to send charges to a Stripe Test account. 

To send charges to your Stripe Test account, replace the following String values (located in com.example.ddoggett.androidpaydemo.HomeActivity.java) with the correct Secret Key and Publishable Key that are associated with your test account.

    public static String STRIPE_SECRET_KEY = "STRIPE_SECRET_KEY";
    public static String STRIPE_PUBLISHABLE_KEY = "STRIPE_PUBLISHABLE_KEY";
