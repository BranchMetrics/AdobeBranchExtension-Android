package io.branch.adobe.sdk;

public class BranchConfig {
    static final String BRANCH_EVENT_NAME_INITIALIZE     = "branch-init";
    static final String BRANCH_EVENT_NAME_SHOWSHARESHEET = "branch-share-sheet";
    static final String BRANCH_EVENT_NAME_DEEPLINKOPENED = "branch-deep-link-opened";

    static final String BRANCH_EVENT_TYPE               = "com.branch.eventType";
    static final String BRANCH_EVENT_SOURCE             = "com.branch.eventSource";

    static final String BRANCH_LINK_TITLEKEY            = "contentTitle";
    static final String BRANCH_LINK_SUMMARYKEY          = "contentDescription";
    static final String BRANCH_LINK_IMAGEURLKEY         = "contentImage";
    static final String BRANCH_LINK_CANONICALURLKEY     = "canonicalURLKey";
    static final String BRANCH_LINK_USERINFOKEY         = "userInfo";
    static final String BRANCH_LINK_CAMPAIGNKEY         = "campaign";
    static final String BRANCH_LINK_TAGSKEY             = "tags";
    static final String BRANCH_LINK_SHARETEXTKEY        = "shareText";
    static final String BRANCH_LINK_ISFIRSTSESSIONKEY   = "isFirstSession";

    static final String BRANCH_CONFIG_BRANCHKEY         = "branchKey";

    static final String BRANCH_DEEPLINK_NOTIFICATION    = "ABEBranchDeepLinkNotification";
}
