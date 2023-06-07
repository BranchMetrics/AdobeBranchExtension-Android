package io.branch.adobe.extension;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.SharedStateResolution;
import com.adobe.marketing.mobile.SharedStateResult;
import com.adobe.marketing.mobile.SharedStateStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.branch.referral.Branch;
import io.branch.referral.BranchPluginSupport;
import io.branch.referral.PrefHelper;
import io.branch.referral.util.BRANCH_STANDARD_EVENT;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.CurrencyType;

public class AdobeBranchExtension extends Extension {
    private static final String TAG = "AdobeBranchExtension::";

    private static final String ADOBE_TRACK_EVENT = "com.adobe.eventType.generic.track";
    private static final String ADOBE_EVENT_SOURCE = "com.adobe.eventSource.requestContent";

    private static final String ADOBE_IDENTITY_EXTENSION = "com.adobe.module.identity";
    private static final String ADOBE_ANALYTICS_EXTENSION = "com.adobe.module.analytics";

    private static final String ADOBE_HUB_EVENT_TYPE = "com.adobe.eventType.hub";
    private static final String ADOBE_SHARED_STATE_EVENT_SOURCE = "com.adobe.eventSource.sharedState";
    private static final String ADOBE_SHARED_STATE_EVENT_OWNER_KEY = "stateowner";

    static final String BRANCH_CONFIGURATION_EVENT = "io.branch.eventType.configuration";
    static final String BRANCH_EVENT_SOURCE = "io.branch.eventsource.configureContent";

    static final String IDENTITY_ID = "mid";
    static final String ANALYTICS_VISITOR_ID = "vid";
    static final String ANALYTICS_TRACKING_ID = "aid";
    static AtomicBoolean PASSED_ADOBE_IDS_TO_BRANCH = new AtomicBoolean(false);

    static List<String> allowList = new ArrayList<>();
    static List<String> exclusionList = new ArrayList<>();

    private List<AdobeBranch.EventTypeSource> apiWhitelist;

    public static final Class<? extends Extension> EXTENSION = AdobeBranchExtension.class;

    protected AdobeBranchExtension(@NonNull ExtensionApi extensionApi) {
        super(extensionApi);
    }

    @Override
    protected String getName() {
        return "io.branch";
    }

    @Override
    protected String getFriendlyName() {
        return "AdobeBranchExtension";
    }

    @Override
    protected @Nullable
    Map<String, String> getMetadata() {
        return null;
    }

    protected void onRegistered() {
        super.onRegistered();

        AdobeBranch.getAutoInstance( MobileCore.getApplication().getApplicationContext());
        Branch.enableLogging();
        registerListeners();

        PrefHelper.Debug("AdobeBranchExtension was successfully registered.");
    }

    private void registerListeners() {
        getApi().registerEventListener(EventType.WILDCARD, EventSource.WILDCARD, this::handleAdobeEvent);
    }

    @Override
    protected void onUnregistered() {
        super.onUnregistered();
    }

    @Override
    public final String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public boolean readyForEvent(Event event) {
        SharedStateResult res = getApi().getSharedState("com.adobe.module.configuration", event, true, SharedStateResolution.ANY);
        return res != null && res.getStatus() == SharedStateStatus.SET;
    }

    void handleAdobeEvent(final Event event) {
        PrefHelper.Debug(TAG + String.format("Started processing new event [%s] of type [%s] and source [%s]",
                branchEventNameFromAdobeEvent(event), event.getType(), event.getSource()));

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
            PrefHelper.Debug(TAG + "Event Dropped: " + branchEventNameFromAdobeEvent(event));
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
     *
     * @param event Adobe Branch Event
     */
    @SuppressWarnings("unchecked")  // Cast Conversion to List<EventTypeSource>
    private void handleBranchConfigurationEvent(final Event event) {
        Map<String, Object> eventData = event.getEventData();
        if (eventData != null) {
            PrefHelper.Debug("Configuring AdobeBranch");

            Object object = eventData.get(AdobeBranch.KEY_APICONFIGURATION);
            ExtensionApi api = getApi();

            // We expect this to be a List of Strings.
            if (object instanceof List<?> && api != null) {
                try {
                    apiWhitelist = (List<AdobeBranch.EventTypeSource>) object;
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
     * extensions that keeps track of Adobe IDs (e.g. from Identity/Analytics extensions). If so, get
     * the shared state of that extension and retrieve the Adobe IDs if they are present, then pass the IDs to Branch.
     * https://aep-sdks.gitbook.io/docs/resources/building-mobile-extensions/requesting-a-shared-state
     *
     * @param event Adobe Event
     */
    private void handleSharedStateEvent(final Event event) {
        Branch branch = Branch.getInstance();
        if (branch != null && event != null && event.getEventData() != null) {
            SharedStateResult extensionSharedState = null;
            Object stateowner = event.getEventData().get(ADOBE_SHARED_STATE_EVENT_OWNER_KEY);
            SharedStateResolution resolution = SharedStateResolution.ANY;

            if (ADOBE_ANALYTICS_EXTENSION.equals(stateowner)) {
                extensionSharedState = getApi().getSharedState(ADOBE_ANALYTICS_EXTENSION, event, true, resolution);
            } else if (ADOBE_IDENTITY_EXTENSION.equals(stateowner)) {
                extensionSharedState = getApi().getSharedState(ADOBE_ANALYTICS_EXTENSION, event, true, resolution);
            }

            if (extensionSharedState != null) {
                for (Map.Entry<String, Object> entry : extensionSharedState.getValue().entrySet()) {
                    PrefHelper.Debug(String.format("identity extension shared state = %s", extensionSharedState.toString()));

                    Object value = entry.getValue();
                    if (value == null) continue;
                    String valueAsString = value.toString();
                    if (TextUtils.isEmpty(valueAsString)) continue;

                    final String key = entry.getKey();
                    switch (key) {
                        case IDENTITY_ID:
                            // pass Adobe Experience Cloud ID (https://app.gitbook.com/@aep-sdks/s/docs/using-mobile-extensions/mobile-core/identity/identity-api-reference#getExperienceCloudIdTitle)
                            branch.setRequestMetadata("$marketing_cloud_visitor_id", valueAsString);
                            break;
                        case ANALYTICS_VISITOR_ID:
                            // pass Adobe Custom Visitor ID (https://aep-sdks.gitbook.io/docs/using-mobile-extensions/adobe-analytics/analytics-api-reference#getvisitoridentifier)
                            branch.setRequestMetadata("$analytics_visitor_id", valueAsString);
                            break;
                        case ANALYTICS_TRACKING_ID:
                            // pass Adobe Tracking ID (https://aep-sdks.gitbook.io/docs/using-mobile-extensions/adobe-analytics/analytics-api-reference#gettrackingidentifier)
                            // if MARKETING_CLOUD_VISITOR_ID is set this will always be null unless the Adobe Launch client set a grace period to support both IDs (https://docs.adobe.com/content/help/en/id-service/using/implementation/setup-analytics.html#:~:text=Grace%20periods%20can%20run%20for,a%20grace%20period%20if%20required.&text=You%20need%20a%20grace%20period,the%20same%20Analytics%20report%20suite.)
                            branch.setRequestMetadata("$adobe_visitor_id", valueAsString);
                            break;
                    }
                    if (IDENTITY_ID.equals(key) || ANALYTICS_VISITOR_ID.equals(key) || ANALYTICS_TRACKING_ID.equals(key)) {
                        // we received at least one, non-empty adobe id
                        PASSED_ADOBE_IDS_TO_BRANCH.set(true);
                        Branch.getInstance().removeSessionInitializationDelay();
                    }
                }
            }
        }
    }

    /**
     * Handle the Adobe Track Event.
     * This is sent by Adobe, in response to a dispatchEvent
     *
     * @param event Adobe Event
     */
    private void handleEvent(final Event event) {
        String name = branchEventNameFromAdobeEvent(event);
        if (!isValidEventForBranch(name)) return;

        BranchEvent branchEvent = branchEventFromAdobeEvent(event);
        if (branchEvent != null) {
            try {
                PrefHelper.Debug(TAG + "Track BranchEvent: " + branchEvent.getEventName());

                branchEvent.logEvent(MobileCore.getApplication().getApplicationContext());

                deviceDataSharedState(event);
            } catch (Exception e) {
                PrefHelper.LogAlways(TAG + "handleTrackEvent Exception" + e.getMessage());
            }
        }
    }

    private BranchEvent branchEventFromAdobeEvent(final Event event) {
        BranchEvent branchEvent = null;
        Map<String, Object> eventData = event.getEventData();
        if (eventData != null) {

            // get Branch event name
            String name = branchEventNameFromAdobeEvent(event);

            try {
                // Try to make a Standard Branch Event if possible
                BRANCH_STANDARD_EVENT eventType = BRANCH_STANDARD_EVENT.valueOf(name);
                branchEvent = new BranchEvent(eventType);
            } catch (IllegalArgumentException e) {
                //This is expected if we are unable to create a Standard Event, default to Custom Event
                branchEvent = new BranchEvent(name);
            }

            // Map Adobe's event data to Branch event properties
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

    public static String branchEventNameFromAdobeEvent(Event event) {
        String name = event.getName();
        Map<String, Object> eventData = event.getEventData();
        if (eventData == null) return name;

        if (eventData.containsKey("action")) {
            name = eventData.get("action").toString();
        } else if (eventData.containsKey("state")) {
            name = eventData.get("state").toString();
        }
        return name;
    }

    private boolean isValidEventForBranch(String eventName) {
        if (allowList.isEmpty() && exclusionList.isEmpty()) {
            return true;
        } else if (!allowList.isEmpty() && allowList.contains(eventName)) {
            return true;
        } else if (!exclusionList.isEmpty() && !exclusionList.contains(eventName)) {
            return true;
        }

        return false;
    }

    /**
     * Add a Branch "Standard" Key/Value to a Branch Event
     *
     * @param event Branch Event
     * @param key   Key
     * @param value Value
     * @return true if the key maps directly to a well known Branch Event key
     */
    private boolean addStandardProperty(BranchEvent event, String key, Object value) {
        switch (key) {
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
     *
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

    private void deviceDataSharedState(Event event) {
        ExtensionApi api = getApi();
        if (api != null) {
            Map<String, Object> deviceData = BranchPluginSupport.getInstance().deviceDescription();
            api.createSharedState(deviceData, event);
        }
    }

    public static void configureEventAllowList(List<String> eventNames) {
        if (!eventNames.isEmpty() && !exclusionList.isEmpty()) {
            throw new IllegalArgumentException("Already configured exclusionList for AdobeBranchExtension");
        }

        allowList = eventNames;
    }

    public static void configureEventExclusionList(List<String> eventNames) {
        if (!eventNames.isEmpty() && !allowList.isEmpty()) {
            throw new IllegalArgumentException("Already configured allowList for AdobeBranchExtension");
        }

        exclusionList = eventNames;
    }
}
