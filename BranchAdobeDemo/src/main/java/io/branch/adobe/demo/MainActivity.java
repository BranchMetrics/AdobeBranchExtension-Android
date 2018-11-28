package io.branch.adobe.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.MobileCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
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

    private void initForm() {
        findViewById(R.id.purchase).setOnClickListener(this);
    }

    private void doPurchase() {
        Log.d(TAG, "doPurchase()");
        Long timestamp = System.currentTimeMillis()/1000;

        Map<String, Object> eventData = new HashMap<String, Object>();
        eventData.put("revenue", 200.00);
        eventData.put("currency", "USD");
        eventData.put("timestamp", timestamp.toString());
        eventData.put("category", "Arts & Entertainment");
        eventData.put("sku", "sku-be-doo");

        Event newEvent = new Event.Builder("Test Purchase Event",
                "com.adobe.eventType.generic.track",
                "com.adobe.eventSource.requestContent")
                .setEventData(eventData).build();

        // dispatch the analytics event
        ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
            @Override
            public void error(final ExtensionError extensionError) {
                Log.e(TAG, String.format("An error occurred while dispatching event %d %s", extensionError.getErrorCode(), extensionError.getErrorName()));
            }
        };
        MobileCore.dispatchEvent(newEvent, errorCallback);
    }
}
