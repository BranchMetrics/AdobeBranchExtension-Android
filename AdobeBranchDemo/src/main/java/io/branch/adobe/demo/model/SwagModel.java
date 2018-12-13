package io.branch.adobe.demo.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SwagModel {
    private int mId;
    private String mTitle;
    private String mDescription;
    private double mPrice;

    /**
     * Constructor.
     * @param jsonObject JSONObject
     */
    public SwagModel(JSONObject jsonObject) {
        try {
            mId = jsonObject.getInt("id");
            mTitle = jsonObject.getString("title");
            mDescription = jsonObject.getString("description");
            mPrice = jsonObject.getDouble("price");
        } catch (JSONException e) {
        }
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public double getPrice() {
        return mPrice;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append(JSONPair("id", getId()));
        sb.append(",");

        sb.append(JSONPair("title", getTitle()));
        sb.append(",");

        sb.append(JSONPair("description", getDescription()));
        sb.append(",");

        sb.append(JSONPair("price", getPrice()));

        sb.append("}");

        return sb.toString();
    }

    private String JSONPair(String key, String value) {
        return quote(key) + ":" + quote(value);
    }
    private String JSONPair(String key, Number value) {
        return quote(key) + ":" + value;
    }

    private String quote(String str) {
        return "\"" + str + "\"";
    }

    /**
     * Import a Swag Catalog (JSON) and convert it to a list of models.
     * @param jsonObject Catalog with an array of Swag
     * @return a list of {@link SwagModel}s
     */
    public static List<SwagModel> importCatalog(JSONObject jsonObject) {
        List<SwagModel> list = new ArrayList<>();
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("catalog");
            for (int i = 0; i < jsonArray.length(); i++) {
                SwagModel model = new SwagModel(jsonArray.getJSONObject(i));
                list.add(model);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

}
