package io.branch.adobe.extension.test;

import android.app.Application;
import android.util.Log;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.InvalidInitException;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Signal;
import com.adobe.marketing.mobile.UserProfile;

import io.branch.adobe.extension.AdobeBranch;
import io.branch.adobe.extension.AdobeBranchExtension;

public class TestApplication extends Application {
    private static final String TAG = "Branch::TestApplication::";
    private static final String ADOBE_APP_ID = "launch-EN1357dc725b8544bd8adc1b4f4ab4c970-development";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize
        initAdobeBranch();
    }

    private void initAdobeBranch() {
        Log.d(TAG, "initAdobeBranch()");

        AdobeBranch.getAutoInstance(this);

        MobileCore.setApplication(this);
        MobileCore.setLogLevel(LoggingMode.VERBOSE);

        try {
            AdobeBranchExtension.registerExtension(this, true);
            UserProfile.registerExtension();
            Identity.registerExtension();
            Lifecycle.registerExtension();
            Signal.registerExtension();
            MobileCore.start(new AdobeCallback () {
                @Override
                public void call(Object o) {
                    MobileCore.configureWithAppID(ADOBE_APP_ID);
                }
            });
        } catch (InvalidInitException e) {
            Log.e(TAG, "InitException", e);
        }
    }
}
