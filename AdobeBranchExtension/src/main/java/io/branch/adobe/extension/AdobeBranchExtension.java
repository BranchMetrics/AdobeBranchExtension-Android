package io.branch.adobe.extension;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;

import java.lang.reflect.Field;
import java.util.Map;

import io.branch.referral.Branch;
import io.branch.referral.util.BRANCH_STANDARD_EVENT;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.CurrencyType;

public class AdobeBranchExtension extends Extension implements ExtensionErrorCallback<ExtensionError> {
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

    @Override
    public void error(ExtensionError extensionError) {
        // something went wrong...
        // TODO: Figure out what to do about it.
        Log.e(TAG, String.format("An error occurred in the AdobeBranchExtension %d %s", extensionError.getErrorCode(), extensionError.getErrorName()));
    }

    private void initExtension() {
        // Register an Event Listener for track events
        getApi().registerEventListener("com.adobe.eventtype.generic.track", "com.adobe.eventsource.requestcontent", AdobeBranchExtensionListener.class, this);

        // Register a Wildcard Listener
        // getApi().registerWildcardListener(AdobeBranchExtensionListener.class, this);
    }

    // Package Private
    void handleAdobeEvent(final Event event) {
        Log.d(TAG, String.format("Started processing new event [%s] of type [%s] and source [%s]", event.getName(), event.getType(), event.getSource()));

        if (isAdobeInitEvent(event)) {
            handleBranchInitEvent(event);
        }

        if (Branch.getInstance() == null) {
            // Branch is not initialized.
            return;
        }

        if (isAdobeTrackEvent(event)) {
            handleTrackEvent(event);
        }

        // TODO: Handle Other Events
    }

    private boolean isAdobeInitEvent(final Event event) {
        return (event.getType().equals("com.adobe.eventtype.configuration") && event.getSource().equals("com.adobe.eventsource.responsecontent"));
    }

    private boolean isAdobeTrackEvent(final Event event) {
        return (event.getType().equals("com.adobe.eventtype.generic.track") && event.getSource().equals("com.adobe.eventsource.requestcontent"));
    }

    /**
     * Handle the Branch Init Event.
     * This is sent by Adobe, when configuration data is available.
     * TODO: Revisit, as this is only applicable in late initialization cases.
     * @param event Adobe Event
     */
    private void handleBranchInitEvent(final Event event) {
        if (Branch.getInstance() != null) {
            Log.i(TAG, "Branch already initialized");
            return;
        }

        Map<String, Object> configuration = this.getApi().getSharedEventState("com.adobe.module.configuration", event, this);
        if (configuration == null) {
            Log.e(TAG, "Branch Configuration not found");
            return;
        }

        String branchKey = String.valueOf(configuration.get(BranchConfig.BRANCH_CONFIG_BRANCHKEY));
        if (TextUtils.isEmpty(branchKey)) {
            Log.e(TAG, "Branch Key not found");
            return;
        }

        Context context = getAdobeContext();
        if (context == null) {
            Log.e(TAG, "Application Context not found");
            return;
        }

        // Initialize Branch
        Branch.enableLogging();
        Branch.enableForcedSession();   // TODO: Revisit why this is required for late-initialization
        Branch.getInstance(context, branchKey);

        enumerateMap("init", configuration);
        Log.d(TAG, "Branch Initialized.");
    }

    /**
     * Handle the Adobe Track Event.
     * This is sent by Adobe, in response to a dispatchEvent
     * @param event Adobe Event
     */
    private void handleTrackEvent(final Event event) {
        BranchEvent branchEvent = branchEventFromAdobeEvent(event);
        if (branchEvent != null) {
            Log.d(TAG, "=== logEvent(Start)");
            try {
                branchEvent.logEvent(getAdobeContext());
            } catch(Exception e) {
                Log.e(TAG, "EXCEPTION", e);
            }
            Log.d(TAG, "=== logEvent(End)");
        }
    }

    private BranchEvent branchEventFromAdobeEvent(final Event event) {
        BranchEvent branchEvent = null;
        Map<String, Object> eventData = event.getEventData();
        if (eventData != null) {
            enumerateMap("track", eventData);

            try {
                // Try to make a Standard Event if possible.
                BRANCH_STANDARD_EVENT eventType = BRANCH_STANDARD_EVENT.valueOf(event.getName());
                branchEvent = new BranchEvent(eventType);
            } catch (IllegalArgumentException e) {
                //This is expected if we are unable to create a Standard Event
            }

            if (branchEvent == null) {
                // This is considered a "Custom" event
                branchEvent = new BranchEvent(event.getName());
            }


            for (Map.Entry<String, Object> pair : eventData.entrySet()) {
                String key = pair.getKey();
                Object obj = pair.getValue();

                if (!addStandardProperty(branchEvent, key, obj)) {
                    branchEvent.addCustomDataProperty(key, obj.toString());
                }
            }
        }
        return branchEvent;
    }

    /**
     * Add a Branch "Standard" Key/Value to a Branch Event
     * @param event Branch Event
     * @param key Key
     * @param value Value
     * @return true if the key maps directly to a well known Branch Event key
     */
    private boolean addStandardProperty(BranchEvent event, String key, Object value) {
        switch(key) {
            case AdobeBranch.KEY_AFFILIATION:
                event.setAffiliation(value.toString());
                return true;

            case AdobeBranch.KEY_COUPON:
                event.setCoupon(value.toString());
                return true;

            case AdobeBranch.KEY_CURRENCY:
                CurrencyType ct = CurrencyType.getValue(value.toString());
                if (ct != null) {
                    event.setCurrency(ct);
                    return true;
                }
                break;

            case AdobeBranch.KEY_DESCRIPTION:
                event.setDescription(value.toString());
                return true;

            case AdobeBranch.KEY_REVENUE:
                Double revenue = asDouble(value);
                if (revenue != null) {
                    event.setRevenue(revenue);
                    return true;
                }
                break;

            case AdobeBranch.KEY_SEARCH_QUERY:
                event.setSearchQuery(value.toString());
                return true;

            case AdobeBranch.KEY_SHIPPING:
                Double shipping = asDouble(value);
                if (shipping != null) {
                    event.setShipping(shipping);
                    return true;
                }
                break;

            case AdobeBranch.KEY_TAX:
                Double tax = asDouble(value);
                if (tax != null) {
                    event.setShipping(tax);
                    return true;
                }
                break;

            case AdobeBranch.KEY_TRANSACTION_ID:
                event.setTransactionID(value.toString());
                return true;

            default:
                break;
        }

        return false;
    }

    /**
     * Attempt to convert an Object to a Double
     * @param o Object that is hopefully a Number
     * @return a Double if success, or null if the object was not a Number
     */
    private Double asDouble(Object o) {
        Double val = null;
        if (o instanceof Number) {
            val = ((Number) o).doubleValue();
        }
        return val;
    }

    // Note that Adobe is currently evaluating a method to access Context with direct method calls.
    private Context getAdobeContext() {
        Context context = null;
        try {
            Class cls = Class.forName("com.adobe.marketing.mobile.App");
            Field appContext = cls.getDeclaredField("appContext");
            appContext.setAccessible(true);

            context = (Context)appContext.get(null);
        } catch (Exception e) {
            Log.e(TAG, "Unable to dig Context out", e);
        }

        return context;
    }

    // TODO: Debug Code.  Remove before shipping.
    private void enumerateMap(String tag, Map<String, Object> map) {
        Log.d(TAG, "===" + tag + "====================");
        for (Map.Entry<String, Object> pair : map.entrySet()) {
            Log.d(TAG, "Key: " + pair.getKey() + "\t" + pair.getValue().toString());
        }
        Log.d(TAG, "===" + tag + "====================");
    }

}
