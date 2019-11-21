package io.branch.adobe.extension;

import android.content.Context;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.branch.referral.Branch;
import io.branch.referral.PrefHelper;
import io.branch.referral.util.BRANCH_STANDARD_EVENT;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.CurrencyType;

public class AdobeBranchExtension extends Extension implements ExtensionErrorCallback<ExtensionError> {
    private static final String TAG = "AdobeBranchExtension::";

    private static final String ADOBE_TRACK_EVENT = "com.adobe.eventtype.generic.track";
    private static final String ADOBE_EVENT_SOURCE = "com.adobe.eventsource.requestcontent";

    private static final String ADOBE_IDENTITY_EXTENSION = "com.adobe.module.identity";
    private static final String ADOBE_ANALYTICS_EXTENSION = "com.adobe.module.analytics";

    private static final String ADOBE_HUB_EVENT_TYPE = "com.adobe.eventtype.hub";
    private static final String ADOBE_SHARED_STATE_EVENT_SOURCE = "com.adobe.eventsource.sharedstate";
    private static final String ADOBE_SHARED_STATE_EVENT_OWNER_KEY = "stateowner";

    static final String BRANCH_CONFIGURATION_EVENT = "io.branch.eventtype.configuration";
    static final String BRANCH_EVENT_SOURCE = "io.branch.eventsource.configurecontent";

    private List<AdobeBranch.EventTypeSource> apiWhitelist;

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
    public void error(ExtensionError extensionError) {
        // something went wrong...
        // TODO: What to do about it.
        PrefHelper.Debug(TAG + String.format("An error occurred in the AdobeBranchExtension %d %s", extensionError.getErrorCode(), extensionError.getErrorName()));
    }

    private void initExtension() {
        // Register default Event Listeners
        registerExtension(ADOBE_TRACK_EVENT, ADOBE_EVENT_SOURCE);
        registerExtension(BRANCH_CONFIGURATION_EVENT, BRANCH_EVENT_SOURCE);
        registerExtension(ADOBE_HUB_EVENT_TYPE, ADOBE_SHARED_STATE_EVENT_SOURCE);
    }

    private void registerExtension(String eventType, String eventSource) {
        getApi().registerEventListener(eventType, eventSource, AdobeBranchExtensionListener.class, this);
    }

    // Package Private
    void handleAdobeEvent(final Event event) {
        PrefHelper.Debug(TAG + String.format("Started processing new event [%s] of type [%s] and source [%s]", event.getName(), event.getType(), event.getSource()));

        if (Branch.getInstance() == null) {
            // Branch is not initialized.
            return;
        }

        if (isBranchConfigurationEvent(event)) {
            handleBranchConfigurationEvent(event);
        } else if (isSharedStateEvent(event)) {
            handleSharedStateEvent(event);
        } else if (isTrackedEvent(event)) {
            handleEvent(event);
        } else {
            PrefHelper.Debug(TAG + "Event Dropped: " + event.getName());
        }
    }

    private boolean isTrackedEvent(final Event event) {
        if (apiWhitelist == null) {
            return (event.getType().equals(ADOBE_TRACK_EVENT) && event.getSource().equals(ADOBE_EVENT_SOURCE));
        }

        for (AdobeBranch.EventTypeSource pair : apiWhitelist) {
            if (pair.getType().equals(event.getType()) && pair.getSource().equals(event.getSource())) {
                return true;
            }
        }

        return false;
    }

    private boolean isBranchConfigurationEvent(final Event event) {
        return (event.getType().equals(BRANCH_CONFIGURATION_EVENT) && event.getSource().equals(BRANCH_EVENT_SOURCE));
    }

    private boolean isSharedStateEvent(final Event event) {
        return (event.getType().equals(ADOBE_HUB_EVENT_TYPE) && event.getSource().equals(ADOBE_SHARED_STATE_EVENT_SOURCE));
    }

    /**
     * Handle the Branch Configuration Event
     * This is sent by Adobe, in response to {@link AdobeBranch} Configuration methods
     * @param event Adobe Branch Event
     */
    @SuppressWarnings("unchecked")  // Cast Conversion to List<EventTypeSource>
    private void handleBranchConfigurationEvent(final Event event) {
        Map<String, Object> eventData = event.getEventData();
        if (eventData != null) {
            PrefHelper.Debug("Configuring AdobeBranch");

            Object object = eventData.get(AdobeBranch.KEY_APICONFIGURATION);

            // We expect this to be a List of Strings.
            if (object instanceof List<?>) {
                try {
                    apiWhitelist = (List<AdobeBranch.EventTypeSource>)object;

                    // For each pair in the whitelist, register the extension
                    for (AdobeBranch.EventTypeSource pair : apiWhitelist) {
                        registerExtension(pair.getType(), pair.getSource());
                    }

                } catch (Exception e) {
                    // Internal Error.
                    PrefHelper.LogAlways(TAG + "handleBranchConfigurationEvent Exception" + e.getMessage());
                }
            } else if (object == null) {
                apiWhitelist = null;
            }
        }
    }

    /**
     * Handle "shared state change" events by checking if the owner of a given event is one of the
     * extensions that keeps track of Adobe IDs (e.g. Identity/Analytics). If so, get the shared state of
     * that extension and retrieve the Adobe IDs if they are present, then pass the IDs to Branch.
     * https://aep-sdks.gitbook.io/docs/resources/building-mobile-extensions/requesting-a-shared-state
     * @param event Adobe Event
     */
    private void handleSharedStateEvent(final Event event) {
        final Branch branch = Branch.getInstance();
        if (branch != null && event != null && event.getEventData() != null) {
            Map<String, Object> extensionSharedState = new HashMap<>();
            Object stateowner = event.getEventData().get(ADOBE_SHARED_STATE_EVENT_OWNER_KEY);
            if (ADOBE_ANALYTICS_EXTENSION.equals(stateowner)) {
                extensionSharedState = getApi().getSharedEventState(ADOBE_ANALYTICS_EXTENSION, event, this);
                PrefHelper.Debug(String.format("analytics extension shared state = %s", new JSONObject(extensionSharedState)));
            } else if (ADOBE_IDENTITY_EXTENSION.equals(stateowner)) {
                extensionSharedState = getApi().getSharedEventState(ADOBE_IDENTITY_EXTENSION, event, this);
                PrefHelper.Debug(String.format("identity extension shared state = %s", new JSONObject(extensionSharedState)));
            }
            for (Map.Entry<String, Object> entry :extensionSharedState.entrySet()) {
                Object value = entry.getValue();
                if (value == null || value.toString().length() == 0 || "null".equalsIgnoreCase(value.toString())) continue;
                // pass
                switch (entry.getKey()) {
                    case "mid":
                        // pass Adobe Experience Cloud ID (https://app.gitbook.com/@aep-sdks/s/docs/using-mobile-extensions/mobile-core/identity/identity-api-reference#getExperienceCloudIdTitle)
                        branch.setRequestMetadata("$marketing_cloud_visitor_id", entry.getValue().toString());
                        break;
                    case "vid":
                        // pass Adobe Custom Visitor ID (https://aep-sdks.gitbook.io/docs/using-mobile-extensions/adobe-analytics/analytics-api-reference#getvisitoridentifier)
                        branch.setRequestMetadata("$analytics_visitor_id", entry.getValue().toString());
                        break;
                    case "aid":
                        // pass Adobe Tracking ID (https://aep-sdks.gitbook.io/docs/using-mobile-extensions/adobe-analytics/analytics-api-reference#gettrackingidentifier)
                        branch.setRequestMetadata("$adobe_visitor_id", entry.getValue().toString());
                        break;
                }
            }
        }
    }

    /**
     * Handle the Adobe Track Event.
     * This is sent by Adobe, in response to a dispatchEvent
     * @param event Adobe Event
     */
    private void handleEvent(final Event event) {
        BranchEvent branchEvent = branchEventFromAdobeEvent(event);
        if (branchEvent != null) {
            try {
                PrefHelper.Debug(TAG + "Track BranchEvent: " + branchEvent.getEventName());

                branchEvent.logEvent(getAdobeContext());
            } catch(Exception e) {
                PrefHelper.LogAlways(TAG + "handleTrackEvent Exception" + e.getMessage());
            }
        }
    }

    private BranchEvent branchEventFromAdobeEvent(final Event event) {
        BranchEvent branchEvent = null;
        Map<String, Object> eventData = event.getEventData();
        if (eventData != null) {
            try {
                // Try to make a Standard Event if possible.
                BRANCH_STANDARD_EVENT eventType = BRANCH_STANDARD_EVENT.valueOf(event.getName());
                branchEvent = new BranchEvent(eventType);
            } catch (IllegalArgumentException e) {
                //This is expected if we are unable to create a Standard Event
            }

            if (branchEvent == null) {
                // This is considered a "Custom" event.
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
                    event.setTax(tax);
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
            PrefHelper.LogAlways(TAG + "Unable to obtain Context");
        }

        return context;
    }
}
