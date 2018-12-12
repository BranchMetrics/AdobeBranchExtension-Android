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

    // TODO; Revisit.  This does not do anything that Branch doesn't already do.
    public static Branch getAutoInstance(@NonNull Context context) {
        return Branch.getAutoInstance(context);
    }

    // TODO; Revisit.  This does not do anything that Branch doesn't already do.
    public static boolean initSession(Branch.BranchReferralInitListener callback) {
        Branch branch = Branch.getInstance();
        if (branch != null) {
            return branch.initSession(callback);
        }
        return false;
    }
}