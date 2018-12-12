package io.branch.sample.testadobebranch;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.branch.adobe.demo.model.SwagModel;
import io.branch.sample.testadobebranch.util.AssetUtils;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TestDataModel extends BaseTest {
    @Test
    public void testSwagModel() throws Throwable {
        JSONObject jsonObject = AssetUtils.readJsonFile(getTestContext(), "swag_data.json");

        List<SwagModel> swagList = SwagModel.importCatalog(jsonObject);
        Assert.assertNotNull(swagList);
        Assert.assertTrue(swagList.size() > 1);

        for (SwagModel model : swagList) {
            Log.d("Branch TestDataModel", model.toString());
        }
    }
}
