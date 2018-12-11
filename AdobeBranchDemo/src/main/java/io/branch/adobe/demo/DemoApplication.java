package io.branch.adobe.demo;

import android.app.Application;
import android.util.Log;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.InvalidInitException;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Signal;
import com.adobe.marketing.mobile.UserProfile;

import java.util.HashMap;
import java.util.Map;

import io.branch.adobe.sdk.AdobeBranchExtension;

public class DemoApplication extends Application {
    private static final String TAG = "DemoApplication::";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize
        initAdobeBranch();
        registerAdobeBranchExtension();
    }

    private void initAdobeBranch() {
        Log.d(TAG, "initAdobeBranch()");
        MobileCore.setApplication(this);
        MobileCore.setLogLevel(LoggingMode.DEBUG);

        try {
            UserProfile.registerExtension();
            Identity.registerExtension();
            Lifecycle.registerExtension();
            Signal.registerExtension();
            MobileCore.start(new AdobeCallback () {
                @Override
                public void call(Object o) {
                    // MobileCore.configureWithAppID("launch-ENf4e5fbcc0c5945de846341e9332df247-development");
                    configureWithTestData();
                }
            });
        } catch (InvalidInitException e) {
            Log.e(TAG, "InitException", e);
        }
    }

    private void registerAdobeBranchExtension() {
        MobileCore.setApplication(this);

        ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
            @Override
            public void error(final ExtensionError extensionError) {
                Log.e(TAG, String.format("An error occurred while registering the AdobeBranchExtension %d %s", extensionError.getErrorCode(), extensionError.getErrorName()));
            }
        };

        if (!MobileCore.registerExtension(AdobeBranchExtension.class, errorCallback)) {
            Log.e(TAG, "Failed to register the AdobeBranchExtension extension");
        }
    }

    private void configureWithTestData() {
        Map<String, Object> config = new HashMap<>();

        // ============================================================
        // global
        // ============================================================
        config.put("global.privacy", "optedin");
        config.put("global.ssl", true);

        // ============================================================
        // Branch
        // ============================================================
        config.put("branchKey", "key_live_nbB0KZ4UGOKaHEWCjQI2ThncEAeRJmhy");

        // ============================================================
        // acquisition
        // ============================================================
        config.put("acquisition.appid", "");
        config.put("acquisition.server", "");
        config.put("acquisition.timeout", 0);

        // ============================================================
        // analytics
        // ============================================================
        config.put("analytics.aamForwardingEnabled", false);
        config.put("analytics.batchLimit", 0);
        config.put("analytics.offlineEnabled", true);
        config.put("analytics.rsids", "");
        config.put("analytics.server", "");
        config.put("analytics.referrerTimeout", 0);

        // ============================================================
        // audience manager
        // ============================================================
        config.put("audience.server", "");
        config.put("audience.timeout", 0);

        // ============================================================
        // identity
        // ============================================================
        config.put("experienceCloud.server", "");
        config.put("experienceCloud.org", "");
        config.put("identity.adidEnabled", false);

        // ============================================================
        // target
        // ============================================================
        config.put("target.clientCode", "");
        config.put("target.timeout", 0);

        // ============================================================
        // lifecycle
        // ============================================================
        config.put("lifecycle.sessionTimeout", 0);
        config.put("lifecycle.backdateSessionInfo", false);

        // ============================================================
        // rules engine
        // ============================================================
        // config.put("rules.url", "https://assets.adobedtm.com/staging/launch-EN9ec4c2c17eab4160bea9480945cdeb4d-development-rules.zip";
        config.put("rules.url", "https://assets.adobedtm.com/staging/launch-EN23ef0b4732004b088acea70c57a44fe2-development-rules.zip");
        config.put("com.branch.extension/deepLinkKey", "pictureId");
        config.put("deepLinkKey", "pictureId");

        MobileCore.updateConfiguration(config);
    }

}
