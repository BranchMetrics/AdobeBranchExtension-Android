package io.branch.sample.libadobebranch;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.InvalidInitException;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Signal;
import com.adobe.marketing.mobile.UserProfile;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.branch.adobe.extension.AdobeBranch;
import io.branch.adobe.extension.AdobeBranchExtension;

/**
 * Adobe Event Testing
 */
@RunWith(AndroidJUnit4.class)
public class AdobeEventTest extends AdobeBranchTest {
    private static final String TAG = "AdobeBranchTest";

    // NOTE: This ID is the same development ID as the DemoApplication
    private static final String ADOBE_APP_ID = "launch-EN1357dc725b8544bd8adc1b4f4ab4c970-development";

    @Override
    public void setUp() {
        super.setUp();

        initAdobeBranch();
        registerAdobeBranchExtension();
    }

    @Test
    public void testDispatchEvent() {
        // 1) Test a standard adobe event
        Event event = new Event.Builder("TestEvent1",
                "com.adobe.eventType.generic.track",
                "com.adobe.eventSource.requestContent")
                .build();
        sendEvent(event);


//        Assert.assertTrue(MobileCore.dispatchEvent(event,errorCallback));


        // 2) Test a whitelisted branch event
//        event = new Event.Builder("TestEvent2",
//                "io.branch.eventType.generic.track",
//                "io.branch.eventSource.requestContent")
//                .build();
//        sendEvent(event);

//        Assert.assertTrue(MobileCore.dispatchEvent(event, null));
//
//        // 3) Test a non-whitelisted event.  This should be dropped
//        event = new Event.Builder("TestEvent3",
//                "com.test.eventType.generic.track",
//                "com.test.eventSource.requestContent")
//                .build();
//        Assert.assertTrue(MobileCore.dispatchEvent(event, null));
    }

    private void sendEvent(final Event event) {
        final CountDownLatch latch = new CountDownLatch(1);
        Assert.assertTrue(MobileCore.dispatchEventWithResponseCallback(event,
            new AdobeCallback<Event>() {
                @Override
                public void call(Event e) {
                    Log.d(TAG, "EVENT CALLBACK: " + e.getName());
                    latch.countDown();
                }
            },
            new ExtensionErrorCallback<ExtensionError>() {
                @Override
                public void error(ExtensionError extensionError) {
                    Assert.fail();
                }
            }));

        try {
            latch.await(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Assert.fail();
        }


    }

    private void initAdobeBranch() {
        AdobeBranch.getAutoInstance(getTestContext());

        MobileCore.setApplication(getApplication());
        MobileCore.setLogLevel(LoggingMode.VERBOSE);

        try {
            UserProfile.registerExtension();
            Identity.registerExtension();
            Lifecycle.registerExtension();
            Signal.registerExtension();
            MobileCore.start(new AdobeCallback() {
                @Override
                public void call(Object o) {
                    MobileCore.configureWithAppID(ADOBE_APP_ID);
                }
            });
        } catch (InvalidInitException e) {
            Log.e(TAG, "InitException", e);
        }
    }

    private void registerAdobeBranchExtension() {
        ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
            @Override
            public void error(final ExtensionError extensionError) {
                Log.e(TAG, String.format("An error occurred while registering the AdobeBranchExtension %d %s", extensionError.getErrorCode(), extensionError.getErrorName()));
                Assert.fail();
            }
        };

        if (!MobileCore.registerExtension(AdobeBranchExtension.class, errorCallback)) {
            Log.e(TAG, "Failed to register the AdobeBranchExtension extension");
            Assert.fail();
        }
    }

}
