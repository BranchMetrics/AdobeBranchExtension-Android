package io.branch.adobe.sdk;

import android.content.Context;

import io.branch.referral.Branch;

public class AdobeBranch {
    private static ITestInterface mInstance;

    public static ITestInterface init(Context context, String packageName) {
        if (mInstance == null) {
            mInstance = new TestInstance(context, packageName);
        }

        return mInstance;
    }

    public static ITestInterface getInstance() {
        return mInstance;
    }

    public interface ITestInterface {
    }

    private static class TestInstance implements ITestInterface {
        private Branch mInstance;

        TestInstance(Context context, String packageName) {
            mInstance = Branch.getAutoInstance(context, packageName);
        }
    }
}
