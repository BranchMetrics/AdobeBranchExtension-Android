package io.branch.adobe.extension.test;

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
        MobileCore.configureWithAppID(ADOBE_APP_ID);
        MobileCore.setLogLevel(LoggingMode.DEBUG);

        List<Class<? extends Extension>> extensions = new ArrayList<>();
        extensions.add(UserProfile.EXTENSION);
        extensions.add(Analytics.EXTENSION);
        extensions.add(Identity.EXTENSION);
        extensions.add(Lifecycle.EXTENSION);
        extensions.add(Signal.EXTENSION);
        extensions.add(AdobeBranchExtension.EXTENSION);
        MobileCore.registerExtensions(extensions, o -> {
            //Log.d(LOG_TAG, "AEP Mobile SDK is initialized");
        });
    }
}
