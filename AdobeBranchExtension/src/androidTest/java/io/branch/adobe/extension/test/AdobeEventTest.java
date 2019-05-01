package io.branch.adobe.extension.test;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.MobileCore;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import io.branch.adobe.extension.AdobeBranch;
import io.branch.referral.Branch;

/**
 * Adobe Event Testing.
 * Note that this isn't your typical unit test suite, because Adobe doesn't actually call back to
 * if the request succeeded or not, just that it had sufficient information to proceed.  We have
 * taken the path of using the ADB logs to do actual validation, and only doing superficial validation
 * during the unit tests themselves.
 */
@RunWith(AndroidJUnit4.class)
public class AdobeEventTest extends AdobeBranchTest {
    private static final String TAG = "AdobeBranchTest";

    @Override
    public void setUp() {
        super.setUp();
        Assert.assertNotNull(Branch.getInstance(getTestContext()));
    }

    @Test
    public void testDispatchEvent() {
        // Test a standard adobe event.  Logs should show one event sent
        Event event = new Event.Builder("testDispatchEvent",
                "com.adobe.eventType.generic.track",
                "com.adobe.eventSource.requestContent")
                .build();
        sendEvent(event);
    }

    @Test
    public void testWhitelistEvent() {
        // Test a whitelisted event.  Logs should show two events sent
        List<AdobeBranch.EventTypeSource> apiWhitelist = new ArrayList<>();
        apiWhitelist.add(new AdobeBranch.EventTypeSource("com.adobe.eventType.generic.track", "com.adobe.eventSource.requestContent"));
        apiWhitelist.add(new AdobeBranch.EventTypeSource("io.branch.eventType.generic.track", "io.branch.eventSource.requestContent"));

        Assert.assertTrue(AdobeBranch.registerAdobeBranchEvents(apiWhitelist));
        sendWhitelistedEvents("testWhitelistEvent");
    }

    @Test
    public void testEmptyWhitelistEvent() {
        // Test a whitelisted event.  Logs should show zero events sent
        List<AdobeBranch.EventTypeSource> apiWhitelist = new ArrayList<>();

        Assert.assertTrue(AdobeBranch.registerAdobeBranchEvents(apiWhitelist));
        sendWhitelistedEvents("testEmptyWhitelistEvent");
    }

    @Test
    public void testNullWhitelistEvent() {
        // Test a whitelisted event.  Logs should show a single event sent (default case)
        Assert.assertTrue(AdobeBranch.registerAdobeBranchEvents(null));
        sendWhitelistedEvents("testEmptyWhitelistEvent");
    }

    // Utility to send a batch of events
    private void sendWhitelistedEvents(String eventName) {
        int eventId = 0;
        Event event = new Event.Builder(eventName + ++eventId,
                "com.adobe.eventType.generic.track",
                "com.adobe.eventSource.requestContent")
                .build();
        sendEvent(event);

        event = new Event.Builder(eventName + ++eventId,
                "io.branch.eventType.generic.track",
                "io.branch.eventSource.requestContent")
                .build();
        sendEvent(event);

        event = new Event.Builder(eventName + ++eventId,
                "com.test.eventType.generic.track",
                "com.test.eventSource.requestContent")
                .build();
        sendEvent(event);
    }

    // Utility to test sending a single event to Adobe with a START and END marker
    private void sendEvent(final Event event) {
        Log.d(TAG, "sendEvent (START) -- " + event.getName());

        Assert.assertTrue(MobileCore.dispatchEventWithResponseCallback(event,
            new AdobeCallback<Event>() {
                @Override
                public void call(Event eventCopy) {
                    Assert.assertEquals(event.getName(), eventCopy.getName());
                }
            },
            new ExtensionErrorCallback<ExtensionError>() {
                @Override
                public void error(ExtensionError extensionError) {
                    Assert.fail();
                }
            }));

        // Adobe is asynchronous...  give it a chance to show something in the logs.
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Assert.fail();
        }

        Log.d(TAG, "sendEvent (END)  -- " + event.getName());
    }
}
