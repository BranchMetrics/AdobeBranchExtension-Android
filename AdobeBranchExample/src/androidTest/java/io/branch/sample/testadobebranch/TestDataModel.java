package io.branch.sample.testadobebranch;

import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.branch.adobe.demo.model.SwagModel;
import io.branch.sample.testadobebranch.util.AssetUtils;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TestDataModel extends BaseTest {

    @Test
    public void testSwagModel() throws Throwable {
        JSONObject jsonObject = AssetUtils.readJsonFile(getTestContext(), "swag_data.json");

        List<SwagModel> swagList = SwagModel.importCatalog(jsonObject);
        Assert.assertNotNull(swagList);
        Assert.assertTrue(swagList.size() > 1);

        for (SwagModel model : swagList) {
            // Convert it to a String and back again to a model
            String jsonString = model.toString();
            JSONObject testObject = new JSONObject(jsonString);

            SwagModel testModel = new SwagModel(testObject);
            Assert.assertEquals(model.getId(), testModel.getId());
        }
    }
}
