package io.branch.adobe.demo;

import android.app.Application;
import android.util.Log;

import com.adobe.marketing.mobile.Analytics;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Signal;
import com.adobe.marketing.mobile.UserProfile;

import java.util.ArrayList;
import java.util.List;

import io.branch.adobe.extension.AdobeBranchExtension;
import io.branch.referral.Branch;
import io.branch.referral.BranchLogger;
import io.branch.referral.PrefHelper;

public class DemoApplication extends Application {
    private static final String ADOBE_APP_ID = "d10f76259195/b0503e1a5dce/launch-9948a3b3a89d-development";
    private static final String TAG = "AdobeBranchExample::";

    @Override
    public void onCreate() {
        super.onCreate();

        Branch.enableLogging(BranchLogger.BranchLogLevel.VERBOSE);

        Analytics.setVisitorIdentifier("custom_identifier_1234"); // to test custom visitor ID (key: "vid")

        // Initialize
        initAdobeBranch();
    }

    private void initAdobeBranch() {
        Log.d(TAG, "initAdobeBranch()");

        MobileCore.setApplication(this);
        MobileCore.configureWithAppID(ADOBE_APP_ID);
        MobileCore.setLogLevel(LoggingMode.VERBOSE);

        // NOTE! following code will enable you to configure exclusion list or allow list, but you can't define both!
        // If you don't configure any, all events will send to Branch which is not ideal!
        // Define the allow list of the events names
        //AdobeBranchExtension.configureEventAllowList(Arrays.asList("VIEW"));

        // Define the exclusion list of the events names
        //AdobeBranchExtension.configureEventExclusionList(Arrays.asList("VIEW"));

        List<Class<? extends Extension>> extensions = new ArrayList<>();
        extensions.add(UserProfile.EXTENSION);
        extensions.add(Analytics.EXTENSION);
        extensions.add(Identity.EXTENSION);
        extensions.add(Lifecycle.EXTENSION);
        extensions.add(Signal.EXTENSION);
        extensions.add(AdobeBranchExtension.EXTENSION);
        MobileCore.registerExtensions(extensions, o -> {
            Log.d(TAG, "AEP Mobile SDK is initialized");
        });

    }
}
