package io.branch.adobe.extension;

import android.content.Context;
import android.support.annotation.NonNull;

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
     * @return A {@link Boolean} value, indicating <i>false</i> if initialization is
     * unsuccessful.
     */
    public static boolean initSession(Branch.BranchReferralInitListener callback) {
        Branch branch = Branch.getInstance();
        if (branch != null) {
            return branch.initSession(callback);
        }
        return false;
    }
}