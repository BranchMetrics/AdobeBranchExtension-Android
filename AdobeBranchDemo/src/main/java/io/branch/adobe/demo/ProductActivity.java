package io.branch.adobe.demo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.branch.adobe.demo.model.SwagModel;
import io.branch.sample.testadobebranch.util.AssetUtils;

public class ProductActivity extends AppCompatActivity {
    private static final String TAG = "Branch::ProductActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        initList();
    }

    private void initList() {
        ListView listView = findViewById(android.R.id.list);
        listView.setEmptyView(findViewById(android.R.id.empty));

        try {
            JSONObject jsonAsset = AssetUtils.readJsonFile(this, "swag_data.json");
            List<SwagModel> swagModelList = SwagModel.importCatalog(jsonAsset);

            ListAdapter customAdapter = new SwagAdapter(this, R.layout.swag_item, swagModelList);
            listView.setAdapter(customAdapter);
        } catch (JSONException e) {
            Log.e(TAG, "Error initializing List", e);
        }
    }


}
