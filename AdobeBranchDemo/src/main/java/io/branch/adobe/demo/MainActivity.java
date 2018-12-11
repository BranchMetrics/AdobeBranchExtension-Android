package io.branch.adobe.demo;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.MobileCore;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.branch.adobe.sdk.AdobeBranch;

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

            case R.id.share:
                doShare();
                break;
        }
    }

    @Override
    public void error(final ExtensionError extensionError) {
        Log.e(TAG, String.format("An error occurred while dispatching event %d %s", extensionError.getErrorCode(), extensionError.getErrorName()));
    }

    private void initForm() {
        findViewById(R.id.purchase).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);
    }

    private void doPurchase() {
        Log.d(TAG, "doPurchase()");
        Long timestamp = System.currentTimeMillis()/1000;

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("affiliation", "Branch Metrics Company Store");
        eventData.put("category", "Arts & Entertainment");
        eventData.put("coupon", "SATURDAY NIGHT SPECIAL");
        eventData.put("currency", "USD");
        eventData.put("description", "Branch Swag Kit");
        eventData.put("revenue", 200.00);
        eventData.put("shipping", 0.99);
        eventData.put("sku", "sku-be-doo");
        eventData.put("tax", 19.99);
        eventData.put("timestamp", timestamp.toString());
        eventData.put("transaction_id", "123");

        eventData.put("custom1", "Custom Data 1");
        eventData.put("custom2", "Custom Data 2");

        Event newEvent = new Event.Builder("PURCHASE",
                "com.adobe.eventType.generic.track",
                "com.adobe.eventSource.requestContent")
                .setEventData(eventData).build();

        // dispatch the analytics event
        MobileCore.dispatchEvent(newEvent, this);
    }

    private void doShare() {
        Log.d(TAG, "doShare()");
        Long timestamp = System.currentTimeMillis()/1000;

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("contentTitle", "Sample Item");

        eventData.put(AdobeBranch.BranchLinkTitleKey, "Branch Adobe Demo");
        eventData.put(AdobeBranch.BranchLinkSummaryKey, "Branch Swag");
        eventData.put(AdobeBranch.BranchLinkCampaignKey, "Sharing");
        eventData.put(AdobeBranch.BranchLinkShareTextKey, "Check out this Branch swag!");

        // Share Sheet needs this activity context
        // TODO: Find a way to get the Activity Context in the extension
        eventData.put(AdobeBranch.BranchActivityContextKey, new WeakReference<Activity>(this));

        Event newEvent = new Event.Builder(AdobeBranch.BranchEvent_ShowShareSheet,
                AdobeBranch.BranchEventType,
                AdobeBranch.BranchEventSource)
                .setEventData(eventData).build();

        // dispatch the share event
        MobileCore.dispatchEvent(newEvent, this);
    }

}
