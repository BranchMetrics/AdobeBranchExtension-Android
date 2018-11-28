package io.branch.adobe.sdk;

import android.util.Log;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.MobileCore;

import java.util.HashMap;
import java.util.Map;

public class AdobeBranchExtension extends Extension {
    private static final String TAG = "AdobeBranchExtension::";

    public AdobeBranchExtension(final ExtensionApi extensionApi) {
        super(extensionApi);

        initExtension();
    }

    @Override
    protected String getName() {
        return "io.branch";
    }

    @Override
    public final String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public void onUnregistered() {
        // this method will be called when the extension is unregistered from the
        // Event Hub in order for you to perform the necessary cleanup

        // TODO: Handle Unregister
    }

    // Package Private
    void handleConfigurationEvent(final Event event) {
        Log.d(TAG, String.format("Started processing new event [%s] of type [%s] and source [%s]", event.getName(), event.getType(), event.getSource()));

        // TODO: Handle Event
    }


    private void initExtension() {
        ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
            @Override
            public void error(final ExtensionError extensionError) {
                // something went wrong, the listener couldn't be registered
                Log.e(TAG, String.format("An error occurred while registering the AdobeBranchExtensionListener %d %s", extensionError.getErrorCode(), extensionError.getErrorName()));

            }
        };

        // Configuration Listener
        // TODO: Revisit Wildcard Listener
        boolean success = getApi().registerEventListener("com.adobe.eventType.configuration",
                "com.adobe.eventSource.requestContent", AdobeBranchExtensionListener.class, errorCallback);

        // Event Listener
        // TODO: Revisit Wildcard Listener
        success &= getApi().registerEventListener("com.adobe.eventType.hub",
                "com.adobe.eventSource.sharedState", AdobeBranchExtensionListener.class, errorCallback);

        // Wildcard Listener
        success &= getApi().registerWildcardListener(AdobeBranchExtensionListener.class, errorCallback);

        // Create a Branch Init event that will fire when Adobe is ready
        Map<String, Object> eventData = new HashMap<String, Object>();
        eventData.put("init_branch", true);

        Event newEvent = new Event.Builder("branch-init",
                BranchConfig.BRANCH_EVENT_TYPE_INIT,
                BranchConfig.BRANCH_EVENT_SOURCE_STANDARD)
                .setEventData(eventData).build();

        // Dispatch the Branch Init event
        success &= MobileCore.dispatchEvent(newEvent, errorCallback);

        Log.d(TAG, "initExtension() success:" + success);
    }
}
