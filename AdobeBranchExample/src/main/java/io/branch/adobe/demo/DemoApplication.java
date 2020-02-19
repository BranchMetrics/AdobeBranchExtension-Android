package io.branch.adobe.demo;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Analytics;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.InvalidInitException;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Signal;
import com.adobe.marketing.mobile.UserProfile;

import android.app.Application;
import io.branch.adobe.extension.AdobeBranchExtension;
import io.branch.referral.*;

public class DemoApplication extends Application {
    private static final String ADOBE_APP_ID = "d10f76259195/b0503e1a5dce/launch-9948a3b3a89d-development";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize
        initAdobeBranch();
        Analytics.setVisitorIdentifier("custom_identifier_1234"); // to test custom visitor ID (key: "vid")
    }

    private void initAdobeBranch() {
        PrefHelper.Debug("initAdobeBranch()");

        MobileCore.setApplication(this);
        MobileCore.setLogLevel(LoggingMode.DEBUG);

        try {
            Analytics.registerExtension();
            UserProfile.registerExtension();
            Identity.registerExtension();
            Lifecycle.registerExtension();
            Signal.registerExtension();
            AdobeBranchExtension.registerExtension(this, true);
            MobileCore.start(new AdobeCallback () {
                @Override public void call(Object o) {
                    MobileCore.configureWithAppID(ADOBE_APP_ID);
                }
            });
        } catch (InvalidInitException e) {
            PrefHelper.Debug("InitException: " + e.getLocalizedMessage());
        }
    }
}
