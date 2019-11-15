package io.branch.adobe.demo;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Analytics;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.InvalidInitException;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Signal;
import com.adobe.marketing.mobile.UserProfile;

import android.app.Application;
import io.branch.adobe.extension.AdobeBranch;
import io.branch.adobe.extension.AdobeBranchExtension;
import io.branch.referral.*;

public class DemoApplication extends Application {
    private static final String TAG = "DemoApplication::";
    private static final String ADOBE_APP_ID = "d10f76259195/b0503e1a5dce/launch-9948a3b3a89d-development";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize
        initBranch();
        initAdobeBranch();
        registerAdobeBranchExtension();
    }

    private void initBranch() {
        Branch.enableDebugMode();

        // TODO: Revisit.  This is how we should encourage customers to initialize Branch using Branch.
        // Branch.getAutoInstance(this);
    }

    private void initAdobeBranch() {
        PrefHelper.Debug("initAdobeBranch()");

        // TODO: Revisit.  We should encourage customers to initialize Branch using Branch.
        AdobeBranch.getAutoInstance(this);

        MobileCore.setApplication(this);
        MobileCore.setLogLevel(LoggingMode.DEBUG);

        try {
            Analytics.registerExtension();
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
            PrefHelper.Debug("InitException: " + e.getLocalizedMessage());
        }
    }

    private void registerAdobeBranchExtension() {
        ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
            @Override
            public void error(final ExtensionError extensionError) {
                PrefHelper.Debug(String.format("An error occurred while registering the AdobeBranchExtension %d %s", extensionError.getErrorCode(), extensionError.getErrorName()));
            }
        };

        if (!MobileCore.registerExtension(AdobeBranchExtension.class, errorCallback)) {
            PrefHelper.Debug("Failed to register the AdobeBranchExtension extension");
        }
    }

}
