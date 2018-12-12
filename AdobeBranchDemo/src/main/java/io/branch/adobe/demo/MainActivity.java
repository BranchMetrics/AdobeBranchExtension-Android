package io.branch.adobe.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.MobileCore;

import java.util.HashMap;
import java.util.Map;

import io.branch.adobe.extension.AdobeBranch;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ExtensionErrorCallback<ExtensionError> {
    private static final String TAG = "Branch MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initForm();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.purchase:
                doPurchase();
                break;
        }
    }

    @Override
    public void error(final ExtensionError extensionError) {
        Log.e(TAG, String.format("An error occurred while dispatching event %d %s", extensionError.getErrorCode(), extensionError.getErrorName()));
    }

    private void initForm() {
        findViewById(R.id.purchase).setOnClickListener(this);
    }

    /**
     * Demonstrate creating an Adobe Event with both "well known" and "custom" keys.
     */
    private void doPurchase() {
        Log.d(TAG, "doPurchase()");
        Long timestamp = System.currentTimeMillis()/1000;

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(AdobeBranch.KEY_AFFILIATION, "Branch Metrics Company Store");
        eventData.put(AdobeBranch.KEY_COUPON, "SATURDAY NIGHT SPECIAL");
        eventData.put(AdobeBranch.KEY_CURRENCY, "USD");
        eventData.put(AdobeBranch.KEY_DESCRIPTION, "Branch Swag Kit");
        eventData.put(AdobeBranch.KEY_REVENUE, 200.00);
        eventData.put(AdobeBranch.KEY_SHIPPING, 0.99);
        eventData.put(AdobeBranch.KEY_TAX, 19.99);
        eventData.put(AdobeBranch.KEY_TRANSACTION_ID, "123");

        eventData.put("category", "Arts & Entertainment");
        eventData.put("sku", "sku-be-doo");
        eventData.put("timestamp", timestamp.toString());

        eventData.put("custom1", "Custom Data 1");
        eventData.put("custom2", "Custom Data 2");

        Event newEvent = new Event.Builder("PURCHASE",
                "com.adobe.eventType.generic.track",
                "com.adobe.eventSource.requestContent")
                .setEventData(eventData).build();

        // dispatch the analytics event
        MobileCore.dispatchEvent(newEvent, this);
    }
}
