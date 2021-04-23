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

import java.util.Arrays;

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

            // NOTE! following code will enable you to configure exclusion list or allow list, but you can't define both!
            // If you don't configure any, all events will send to Branch which is not ideal!
            // Define the allow list of the events names
//            AdobeBranchExtension.configureEventAllowList(Arrays.asList("VIEW"));

            // Define the exclusion list of the events names
            AdobeBranchExtension.configureEventExclusionList(Arrays.asList("VIEW"));

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
