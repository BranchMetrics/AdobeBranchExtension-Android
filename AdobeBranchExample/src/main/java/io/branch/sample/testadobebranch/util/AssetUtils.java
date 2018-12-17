package io.branch.sample.testadobebranch.util;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Asset Utilities.
 */
public class AssetUtils {
    public static JSONObject readJsonFile (Context context, String filename) throws JSONException {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
        } catch (IOException e) {
        }

        return new JSONObject(sb.toString());
    }
}
