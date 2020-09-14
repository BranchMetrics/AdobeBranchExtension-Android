package io.branch.adobe.extension.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.MobileCore;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.branch.adobe.extension.AdobeBranch;
import io.branch.referral.Branch;
import io.branch.referral.PrefHelper;
import io.branch.referral.util.BRANCH_STANDARD_EVENT;

/**
 * Adobe Event Testing. We use reflection to retrieve Branch.instrumentationExtraData_ to see if the event
 * was registered and fired on the Branch side. This isn't a typical test suite and the test pass individually
 * but there seems to co-dependencies, so they don't pass when we run them altogether. Todo fix the co-dependencies.
 */
@RunWith(AndroidJUnit4.class)
public class AdobeEventTest extends AdobeBranchTest {
    private static final String TAG = "AdobeEventTest";
    Map<String, Object> randomData = new HashMap<String, Object>() {{
        put("foo", "bar");
        put("foo1", 1);
    }};

    @Test
    public void testTrackCustomState() throws NoSuchFieldException, IllegalAccessException, InterruptedException, InstantiationException {
        testTrackCustomStateWithExpectation(true);
    }

    @Test
    public void testTrackStandardState() throws NoSuchFieldException, IllegalAccessException, InterruptedException, InstantiationException {
        testTrackStandardStateWithExpectation(true);
    }

    @Test
    public void testTrackCustomAction() throws NoSuchFieldException, IllegalAccessException, InterruptedException, InstantiationException {
        testTrackCustomActionWithExpectation(true);
    }

    @Test
    public void testTrackStandardAction() throws NoSuchFieldException, IllegalAccessException, InterruptedException, InstantiationException {
        testTrackStandardActionWithExpectation(true);
    }

    @Test
    public void testWhitelistOfExclusivelyCustomTypeAndSourceEvents() throws InterruptedException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        // define the whitelist
        Map<String, String> whitelistConfigurations = new HashMap<String, String>() {{
            put("com.adobe.eventType.generic.track1", "com.adobe.eventSource.requestContent1");// custom type and source events
            put("io.branch.eventType.generic.track1", "io.branch.eventSource.requestContent1");// custom type and source events
        }};

        // register the whitelist
        List<AdobeBranch.EventTypeSource> apiWhitelist = new ArrayList<>();
        for (Map.Entry<String, String> entry : whitelistConfigurations.entrySet()) {
            apiWhitelist.add(new AdobeBranch.EventTypeSource(entry.getKey(), entry.getValue()));
        }
        Assert.assertTrue(AdobeBranch.registerAdobeBranchEvents(apiWhitelist));
        Thread.sleep(500);

        // test sending this Adobe event of custom type and source, as either custom or standard Branch event depending on that Adobe events name.
        for (Map.Entry<String, String> entry : whitelistConfigurations.entrySet()) {
            sendCustomEventOfCustomTypeAndSource(entry.getKey(), entry.getValue(), true);
            sendStandardEventOfCustomTypeAndSource(entry.getKey(), entry.getValue(), true);
        }
        // normal adobe events should fail
        testTrackStandardActionWithExpectation(false);
        testTrackCustomActionWithExpectation(false);
        testTrackStandardStateWithExpectation(false);
        testTrackCustomStateWithExpectation(false);
    }

    @Test
    public void testEmptyWhitelist() throws InterruptedException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        // register empty whitelist
        Assert.assertTrue(AdobeBranch.registerAdobeBranchEvents(new ArrayList<AdobeBranch.EventTypeSource>()));
        Thread.sleep(500);

        // test sending Adobe event of custom type and source, should fail
        sendStandardEventOfCustomTypeAndSource("io.branch.eventType.generic.track1", "io.branch.eventSource.requestContent1", false);
        // test sending normal Adobe events, should fail
        testTrackStandardActionWithExpectation(false);
        testTrackCustomActionWithExpectation(false);
        testTrackStandardStateWithExpectation(false);
        testTrackCustomStateWithExpectation(false);
    }

    @Test
    public void testNullWhitelist() throws InterruptedException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        // register empty whitelist
        Assert.assertTrue(AdobeBranch.registerAdobeBranchEvents(null));
        Thread.sleep(1000);

        // test sending Adobe event of custom type and source, should fail
        sendStandardEventOfCustomTypeAndSource("io.branch.eventType.generic.track1", "io.branch.eventSource.requestContent1", false);
        // test sending normal Adobe events, should succeed
        testTrackStandardActionWithExpectation(true);
        testTrackCustomActionWithExpectation(true);
        testTrackStandardStateWithExpectation(true);
        testTrackCustomStateWithExpectation(true);
    }






    // UTILITIES

    protected void sendCustomEventOfCustomTypeAndSource(String type, String source, final boolean branchIsExpectedToRegisterEvent) {
        Event event = new Event.Builder("testCustomEventOfCustomTypeAndSource", type, source).setEventData(randomData).build();
        sendEvent(event, branchIsExpectedToRegisterEvent);
    }

    protected void sendStandardEventOfCustomTypeAndSource(String type, String source, final boolean branchIsExpectedToRegisterEvent) {
        Event event = new Event.Builder("ADD_TO_CART", type, source).setEventData(randomData).build();
        sendEvent(event, branchIsExpectedToRegisterEvent);
    }

    // Send a single event to Adobe with a START and END marker (in logs).
    // Compatible with events of custom type and source, thus uses dispatchEvent rather than MobileCore.trackState/MobileCore.trackAction
    protected void sendEvent(final Event event, final boolean branchIsExpectedToRegisterEvent) {
        PrefHelper.Debug("sendEvent (START) -- " + event.getName());

        Assert.assertTrue(MobileCore.dispatchEvent(event,
                new ExtensionErrorCallback<ExtensionError>() {
                    @Override
                    public void error(ExtensionError extensionError) {
                        Assert.fail();
                    }
                }));

        // Adobe is asynchronous...  give it a chance to show something in the logs.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.fail();
        }

        // depending on what the Adobe event's name was, test whether standard or custom Branch event was fired as well
        try {
            BRANCH_STANDARD_EVENT eventType = BRANCH_STANDARD_EVENT.valueOf(event.getName());
            PrefHelper.Debug("eventType: " + eventType);
            try {
                branchSentStandardEvent(branchIsExpectedToRegisterEvent);
            } catch (NoSuchFieldException | IllegalAccessException | InstantiationException e) {
                Assert.fail();
            }
        } catch (IllegalArgumentException e) {
            try {
                branchSentCustomEvent(branchIsExpectedToRegisterEvent);
            } catch (NoSuchFieldException | IllegalAccessException | InstantiationException ee) {
                Assert.fail();
            }
        }

        PrefHelper.Debug("sendEvent (END)  -- " + event.getName());
    }

    protected void branchSentStandardEvent(final boolean branchIsExpectedToRegisterEvent) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        branchSentEvent("standard", branchIsExpectedToRegisterEvent);
    }

    protected void branchSentCustomEvent(final boolean branchIsExpectedToRegisterEvent) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        branchSentEvent("custom", branchIsExpectedToRegisterEvent);
    }

    protected void branchSentEvent(String urlSignature, final boolean branchIsExpectedToRegisterEvent) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        Branch privateObject = Branch.getInstance();
        Field privateMapField = Branch.class.getDeclaredField("instrumentationExtraData_");
        privateMapField.setAccessible(true);
        ConcurrentHashMap<String, String> instrumentationExtraData_ = (ConcurrentHashMap) privateMapField.get(privateObject);

        Assert.assertNotNull(instrumentationExtraData_);

        Assert.assertEquals(branchIsExpectedToRegisterEvent, oneOfMapKeysContains(instrumentationExtraData_, urlSignature));

        // reset instrumentation
        privateMapField.set(privateObject, new ConcurrentHashMap<String, String>());
        ConcurrentHashMap<String, String> instrumentationExtraData_1 = (ConcurrentHashMap) privateMapField.get(privateObject);
        Assert.assertEquals(0, instrumentationExtraData_1.size());
    }

    protected boolean oneOfMapKeysContains(Map<String, String> map, String word) {
        for (String key : map.keySet()) {
            if (key.contains(word)) return true;
        }
        return false;
    }

    protected void testTrackCustomStateWithExpectation(boolean expectation) throws InterruptedException, NoSuchFieldException, IllegalAccessException, InstantiationException {
        MobileCore.trackState("testTrackState", new HashMap<String, String>());
        Thread.sleep(2000);
        branchSentCustomEvent(expectation);
    }

    protected void testTrackStandardStateWithExpectation(boolean expectation) throws InterruptedException, NoSuchFieldException, IllegalAccessException, InstantiationException {
        MobileCore.trackState("PURCHASE", new HashMap<String, String>());
        Thread.sleep(2000);
        branchSentStandardEvent(expectation);
    }

    protected void testTrackCustomActionWithExpectation(boolean expectation) throws InterruptedException, NoSuchFieldException, IllegalAccessException, InstantiationException {
        MobileCore.trackAction("testTrackCustomAction", new HashMap<String, String>());
        Thread.sleep(2000);
        branchSentCustomEvent(expectation);
    }

    protected void testTrackStandardActionWithExpectation(boolean expectation) throws InterruptedException, NoSuchFieldException, IllegalAccessException, InstantiationException {
        MobileCore.trackAction("ADD_TO_CART", new HashMap<String, String>());
        Thread.sleep(2000);
        branchSentStandardEvent(expectation);
    }
}
