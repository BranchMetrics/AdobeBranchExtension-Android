package io.branch.adobe.extension;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.BRANCH_STANDARD_EVENT;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.CurrencyType;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;

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
        // Wildcard Listener
        boolean success = getApi().registerWildcardListener(AdobeBranchExtensionListener.class, this);
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
        } else if (isBranchShareEvent(event)) {
            handleShareEvent(event);
        }

        // TODO: Handle Other Events
    }

    private boolean isAdobeInitEvent(final Event event) {
        return (event.getType().equals("com.adobe.eventtype.configuration") && event.getSource().equals("com.adobe.eventsource.responsecontent"));
    }

    private boolean isAdobeTrackEvent(final Event event) {
        return (event.getType().equals("com.adobe.eventtype.generic.track") && event.getSource().equals("com.adobe.eventsource.requestcontent"));
    }

    private boolean isBranchShareEvent(final Event event) {
        return (event.getType().equals(AdobeBranch.BranchEventType) && event.getSource().equals(AdobeBranch.BranchEventSource));
    }

    /**
     * Handle the Branch Init Event.
     * This is sent by Adobe, when initialization 
     * @param event
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
        Branch.enableForcedSession();   // Required for late-initialization
        Branch.getInstance(context, branchKey);

        // Check to see if Branch actually initialized
        if (Branch.getInstance() != null) {
            // Initialize a Branch Session
            Branch.getInstance().initSession(new Branch.BranchReferralInitListener() {
                @Override
                public void onInitFinished(JSONObject referringParams, BranchError error) {
                    Log.d(TAG, "JSON: " + referringParams.toString());
                    // TODO: Normally we would expect this to be handled by the app developer
                }
            });

        }

        enumerateMap("init", configuration);
        Log.d(TAG, "Branch Initialized.");
    }

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

    private void handleShareEvent(final Event event) {
        Log.d(TAG, "Share Event");
        Map<String, Object> eventData = event.getEventData();
        if (event.getName().equals(AdobeBranch.BranchEvent_ShowShareSheet)) {
            final BranchUniversalObject buo = new BranchUniversalObject();
            final LinkProperties linkProperties = new LinkProperties();
            linkProperties.addControlParameter("foo", "bar");

            final Activity activity = getActivityContext(eventData);
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buo.showShareSheet(activity, linkProperties, new ShareSheetStyle(activity, "My App name", "My Message Body"), null);
                    }
                });
            }
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

            }

            if (branchEvent == null) {
                // This is a "Custom" event
                branchEvent = new BranchEvent(event.getName());
            }

            Iterator iterator = eventData.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> pair = (Map.Entry)iterator.next();
                String key = pair.getKey();
                Object obj = pair.getValue();
                boolean added = false;

                if (!addStandardProperty(branchEvent, key, obj)) {
                    branchEvent.addCustomDataProperty(key, obj.toString());
                }
            }
        }
        return branchEvent;
    }

    boolean addStandardProperty(BranchEvent event, String key, Object value) {
        switch(key) {
            case "affiliation":
                event.setAffiliation(value.toString());
                return true;

            case "coupon":
                event.setCoupon(value.toString());
                return true;

            case "currency":
                CurrencyType ct = CurrencyType.getValue(value.toString());
                if (ct != null) {
                    event.setCurrency(ct);
                    return true;
                }
                break;

            case "description":
                event.setDescription(value.toString());
                return true;

            case "revenue":
                Double revenue = asDouble(value);
                if (revenue != null) {
                    event.setRevenue(revenue);
                    return true;
                }
                break;

            case "search_query":
                event.setSearchQuery(value.toString());
                return true;

            case "shipping":
                Double shipping = asDouble(value);
                if (shipping != null) {
                    event.setShipping(shipping);
                    return true;
                }
                break;

            case "tax":
                Double tax = asDouble(value);
                if (tax != null) {
                    event.setShipping(tax);
                    return true;
                }
                break;

            case "transaction_id":
                event.setTransactionID(value.toString());
                return true;

            default:
                break;
        }

        return false;
    }

    private Double asDouble(Object o) {
        Double val = null;
        if (o instanceof Number) {
            val = ((Number) o).doubleValue();
        }
        return val;
    }

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

    private Activity getActivityContext(Map<String, Object> eventData) {
        Activity activity = null;
        WeakReference<Activity> activityRef = (WeakReference<Activity>)eventData.get(AdobeBranch.BranchActivityContextKey);
        if (activityRef != null) {
            activity = activityRef.get();
        }

        return activity;
    }

    private void enumerateMap(String tag, Map<String, Object> map) {
        Log.d(TAG, "===" + tag + "====================");
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> pair = (Map.Entry)iterator.next();
            Log.d(TAG, "Key: " + pair.getKey() + "\t" + pair.getValue().toString());
        }
        Log.d(TAG, "===" + tag + "====================");
    }

}
