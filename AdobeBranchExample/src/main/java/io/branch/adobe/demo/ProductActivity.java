package io.branch.adobe.demo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.branch.adobe.demo.model.SwagModel;
import io.branch.adobe.demo.util.AssetUtils;
import io.branch.adobe.extension.AdobeBranch;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.PrefHelper;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;

public class ProductActivity extends AppCompatActivity {
    private List<SwagModel> swagModelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareProduct();
            }
        });

        initList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        AdobeBranch.initSession(branchInitSessionCallback, getIntent().getData(), this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
        AdobeBranch.reInitSession(this, branchInitSessionCallback);
    }

    private void initList() {
        ListView listView = findViewById(android.R.id.list);
        listView.setEmptyView(findViewById(android.R.id.empty));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SwagModel swag = (SwagModel)parent.getAdapter().getItem(position);
                showSwagActivity(swag);
            }
        });

        try {
            JSONObject jsonAsset = AssetUtils.readJsonFile(this, "swag_data.json");
             swagModelList = SwagModel.importCatalog(jsonAsset);

            ListAdapter customAdapter = new SwagAdapter(this, R.layout.swag_item, swagModelList);
            listView.setAdapter(customAdapter);
        } catch (JSONException e) {
            PrefHelper.Debug("Error initializing List: " + e.getLocalizedMessage());
        }
    }

    private Branch.BranchReferralInitListener branchInitSessionCallback = new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                PrefHelper.Debug("initBranchSession, referringParams = " + referringParams + ", error = " + error);
                if (referringParams == null) return;
                try {
                    // You would think that there was an easier way to figure this out than looking at LinkProperties code
                    if (referringParams.has("+clicked_branch_link") && referringParams.getBoolean("+clicked_branch_link")) {
                        String idString = referringParams.optString(SwagActivity.SWAG_ID);
                        if (!TextUtils.isEmpty(idString)) {
                            int swagId = Integer.parseInt(idString);

                            // Launch the Swag Activity
                            SwagModel model = findSwagById(swagId);
                            showSwagActivity(model);
                        }
                    }
                } catch (JSONException e) {
                    // referringParams property doesn't exist
                } catch (NumberFormatException e) {
                    // internal error; id is not a number.
                }
            }
        };


    private void shareProduct() {
        BranchUniversalObject buo = new BranchUniversalObject();
        LinkProperties linkProperties = new LinkProperties();

        buo.showShareSheet(this, linkProperties, new ShareSheetStyle(this, getString(R.string.app_name), getString(R.string.catalog)), null);
    }

    private void showSwagActivity(SwagModel swagModel) {
        if (swagModel != null) {
            Intent intent = new Intent(ProductActivity.this, SwagActivity.class);
            intent.putExtra(SwagActivity.SWAG_DATA, swagModel.toString());
            startActivity(intent);
        }
    }

    private SwagModel findSwagById(int id) {
        for (SwagModel model : swagModelList) {
            if (model.getId() == id) {
                return model;
            }
        }
        return null;
    }

}
