package io.branch.adobe.extension;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.MobileCore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.branch.referral.Branch;

/**
 * AdobeBranch Extension.
 */
public class AdobeBranch {
    // ==== EVENT KEYS =============================================================================
    public static final String KEY_AFFILIATION      = "affiliation";
    public static final String KEY_COUPON           = "coupon";
    public static final String KEY_CURRENCY         = "currency";
    public static final String KEY_DESCRIPTION      = "description";
    public static final String KEY_REVENUE          = "revenue";
    public static final String KEY_SEARCH_QUERY     = "search_query";
    public static final String KEY_SHIPPING         = "shipping";
    public static final String KEY_TAX              = "tax";
    public static final String KEY_TRANSACTION_ID   = "transaction_id";

    // Package Private Configuration Event
    static final String KEY_APICONFIGURATION = "branch_api_configuration";

    /**
     * Singleton method to return the pre-initialized, or newly initialize and return, a singleton
     * object of the type {@link Branch}.
     * @param context A {@link Context} from which this call was made.
     * @return An initialized {@link Branch} object
     */
    public static Branch getAutoInstance(@NonNull Context context) {
        return Branch.getAutoInstance(context);
    }

    /**
     * <p>Initializes a session with the Branch API.
     * @param callback A listener that will be called following successful (or unsuccessful)
     *                 initialization of the session with the Branch API.
     * @param data     A {@link  Uri} variable containing the details of the source link that
     *                 led to this initialization action.
     * @param activity The calling {@link Activity} for context.
     * @return A {@link Boolean} value that will return <i>false</i> if the supplied <i>data</i>
     * parameter cannot be handled successfully - i.e. is not of a valid URI format.
     */
    public static boolean initSession(Branch.BranchReferralInitListener callback, Uri data, Activity activity) {
        Branch branch = Branch.getInstance();
        if (branch != null) {
            return branch.initSession(callback, data, activity);
        }
        return false;
    }

    /**
     * Register a whitelist of Event Types and Event Sources to send to Branch.
     * @param additionalEvents Additional events to listen for.
     *                         If empty, this extension will not listen for any events.
     *                         If null, this extension will default to listen for all Adobe events.
     *                         If non-empty, will listen for only those events that are in the list.
     * @return true if the configuration was successful
     */
    public static boolean registerAdobeBranchEvents(List<EventTypeSource> additionalEvents) {
        Map<String, Object> eventData = new HashMap<>();

        eventData.put(AdobeBranch.KEY_APICONFIGURATION, additionalEvents);

        Event newEvent = new Event.Builder(AdobeBranch.KEY_APICONFIGURATION,
                AdobeBranchExtension.BRANCH_CONFIGURATION_EVENT,
                AdobeBranchExtension.BRANCH_EVENT_SOURCE)
                .setEventData(eventData).build();

        // dispatch the analytics event
        return MobileCore.dispatchEvent(newEvent, null);
    }

    /**
     * Pair for holding an Event Type and Event Source.
     */
    public static class EventTypeSource extends Pair<String, String> {
        /**
         * Constructor.
         *
         * @param type  Event Type
         * @param source Event Source
         */
        public EventTypeSource(String type, String source) {
            super(type.toLowerCase(), source.toLowerCase());
        }

        /**
         * @return the Event Type
         */
        public final String getType() {
            return this.first;
        }

        /**
         * @return the Event Source
         */
        public final String getSource() {
            return this.second;
        }
    }
}
