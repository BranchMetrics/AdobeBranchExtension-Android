package io.branch.adobe.extension.test;

import android.app.Application;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Analytics;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.InvalidInitException;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Signal;
import com.adobe.marketing.mobile.UserProfile;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import io.branch.adobe.extension.AdobeBranchExtension;
import io.branch.referral.Branch;
import io.branch.referral.PrefHelper;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

/**
 * Base Instrumented test, which will execute on an Android device.
 */
@RunWith(AndroidJUnit4.class)
public class AdobeBranchTest {
    private static final String TAG = "AdobeBranchTest";
    private static final String ADOBE_APP_ID = "d10f76259195/b0503e1a5dce/launch-9948a3b3a89d-development";
    private Application app;
    private Context mContext;

    @Rule
    public ActivityTestRule<MockActivity> mActivityRule = new ActivityTestRule<>(MockActivity.class);

    @Before
    public void setUp() {
        app = mActivityRule.getActivity().getApplication();
        mContext = getInstrumentation().getContext();

        initAdobeBranch();
    }

    private void initAdobeBranch() {
        PrefHelper.Debug("AdobeBranchTest.initAdobeBranch()");

        MobileCore.setApplication(app);
        MobileCore.setLogLevel(LoggingMode.DEBUG);

        try {
            Analytics.registerExtension();
            UserProfile.registerExtension();
            Identity.registerExtension();
            Lifecycle.registerExtension();
            Signal.registerExtension();
            AdobeBranchExtension.registerExtension(app, true);
            MobileCore.start(new AdobeCallback() {
                @Override public void call(Object o) {
                    MobileCore.configureWithAppID(ADOBE_APP_ID);
                }
            });
        } catch (InvalidInitException e) {
            PrefHelper.Debug("AdobeBranchTest.InvalidInitException: " + e.getLocalizedMessage());
        }
    }

    @After
    public void tearDown() {
        shutdownBranch();
        app = null;
        mContext = null;
    }

    @Test
    public void testAppContext() {
        // Context of the app under test.
        Assert.assertNotNull(mContext);
    }

    @Test
    public void testBranchInstance() {
        // Context of the app under test.
        Assert.assertNotNull(Branch.getInstance());
    }

    private void shutdownBranch() {
        try {
            Method method = Branch.class.getDeclaredMethod("shutDown");
            method.setAccessible(true);
            method.invoke(null);
        } catch (Exception e) {
            Assert.fail();
        }
    }
}
