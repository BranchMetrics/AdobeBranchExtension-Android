package io.branch.sample.testadobebranch;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class BaseTest {
    private Context mContext;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getContext();
    }

    @After
    public void tearDown() {
        mContext = null;
    }

    @Test
    public void testPackageName() {
        assertEquals("io.branch.adobe.demo.test", mContext.getPackageName());
    }

    Context getTestContext() {
        return mContext;
    }
}
