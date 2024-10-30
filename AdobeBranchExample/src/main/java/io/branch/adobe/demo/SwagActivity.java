package io.branch.adobe.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionHelper;
import com.adobe.marketing.mobile.MobileCore;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.branch.adobe.demo.model.SwagModel;
import io.branch.adobe.extension.AdobeBranch;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;

public class SwagActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Branch::SwagActivity";

    public static final String SWAG_DATA = "swag";
    public static final String SWAG_ID = "swag_id";

    private SwagModel mSwagModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swag);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareProduct();
            }
        });

        findViewById(R.id.add_to_cart).setOnClickListener(this);
        findViewById(R.id.purchase).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String swagData  = extras.getString(SWAG_DATA, null);
            if (swagData != null) {
                init(swagData);
            }
        }

        if (mSwagModel == null) {
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.add_to_cart) {
            doAddToCart(view);
        } else if (id == R.id.purchase) {
            doPurchase(view);
        }
    }
    
//
//    public void error(final ExtensionError extensionError) {
//        Log.e(TAG, String.format("An error occurred while dispatching event %d %s", extensionError.getErrorCode(), extensionError.getErrorName()));
//    }


    private void init(String swagData) {
        try {
            JSONObject jsonObject = new JSONObject(swagData);
            mSwagModel = new SwagModel(jsonObject);
        } catch (JSONException e) {
            // Handled
        }

        if (mSwagModel != null) {
            ImageView image = findViewById(R.id.content_img);
            TextView title = findViewById(R.id.title_txt);
            TextView description = findViewById(R.id.description_txt);
            TextView price = findViewById(R.id.price_txt);

            if (image != null) {
                image.setImageResource(SwagAdapter.findImageResource(mSwagModel.getId()));
            }
            if (title != null) {
                title.setText(mSwagModel.getTitle());
            }
            if (description != null) {
                description.setText(mSwagModel.getDescription());
            }
            if (price != null) {
                price.setText("$" + Double.toString(mSwagModel.getPrice()));
            }
        }
    }

    private void shareProduct() {
        BranchUniversalObject buo = new BranchUniversalObject();
        buo.setContentImageUrl(mSwagModel.getImageUrl());

        LinkProperties linkProperties = new LinkProperties();
        linkProperties.addControlParameter(SWAG_ID, Integer.toString(mSwagModel.getId()));

        buo.showShareSheet(this, linkProperties, new ShareSheetStyle(this, getString(R.string.app_name), mSwagModel.getTitle()), null);
    }

    /**
     * Demonstrate creating an Adobe Event with both "well known" and "custom" keys.
     */
    private void doPurchase(View view) {
        Long timestamp = System.currentTimeMillis()/1000;

        Map<String, String> eventData = new HashMap<>();
        eventData.put(AdobeBranch.KEY_AFFILIATION, "Branch Metrics Company Store");
        eventData.put(AdobeBranch.KEY_COUPON, "SATURDAY NIGHT SPECIAL");
        eventData.put(AdobeBranch.KEY_CURRENCY, "USD");
        eventData.put(AdobeBranch.KEY_DESCRIPTION, mSwagModel.getDescription());
        eventData.put(AdobeBranch.KEY_REVENUE, String.valueOf(mSwagModel.getPrice()));
        eventData.put(AdobeBranch.KEY_SHIPPING, "0.99");
        eventData.put(AdobeBranch.KEY_TAX, String.valueOf(mSwagModel.getPrice() * 0.077));
        eventData.put(AdobeBranch.KEY_TRANSACTION_ID, UUID.randomUUID().toString());

        eventData.put("category", "Arts & Entertainment");
        eventData.put("product_id", String.valueOf(mSwagModel.getId()));
        eventData.put("sku", "sku-be-doo");
        eventData.put("timestamp", timestamp.toString());

        eventData.put("custom1", "Custom Data 1");
        eventData.put("custom2", "Custom Data 2");

        // dispatch the analytics event
        MobileCore.trackAction("PURCHASE", eventData);

        showSnackbar(view, "Thank you for your purchase of " + mSwagModel.getTitle() + "!");
    }

    /**
     * Demonstrate creating an Adobe Event with both "well known" and "custom" keys.
     */
    private void doAddToCart(View view) {
        Long timestamp = System.currentTimeMillis()/1000;

        Map<String, String> eventData = new HashMap<>();
        eventData.put(AdobeBranch.KEY_DESCRIPTION, mSwagModel.getDescription());
        eventData.put(AdobeBranch.KEY_REVENUE, String.valueOf(mSwagModel.getPrice()));
        eventData.put("product_id", String.valueOf(mSwagModel.getId()));
        eventData.put("timestamp", timestamp.toString());
        MobileCore.trackState("ADD_TO_CART", eventData);

        showSnackbar(view, mSwagModel.getTitle() + " added to your shopping cart");
    }

    private void showSnackbar(View view, String txt) {
        Snackbar.make(view, txt, Snackbar.LENGTH_LONG).show();
    }
}
