package io.branch.adobe.extension.test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import io.branch.referral.Branch;

/**
 * Base Instrumented test, which will execute on an Android device.
 */
@RunWith(AndroidJUnit4.class)
public class AdobeBranchTest {
    private Context mContext;

    @Before
    public void setUp() {
        Branch.enableDebugMode();
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @After
    public void tearDown() {
        mContext = null;
        shutdownBranch();
    }

    @Test
    public void testAppContext() {
        // Context of the app under test.
        Assert.assertNotNull(getTestContext());
    }

    Context getTestContext() {
        return mContext;
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
