package io.branch.adobe.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Analytics;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.VisitorID;
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
    private static final String TAG = "Branch::ProductActivity";
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
        initBranchSession();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Identity.getIdentifiers(new AdobeCallback<List<VisitorID>>() {
//                    @Override
//                    public void call(List<VisitorID> visitorIDS) {
//                        PrefHelper.Debug("getIdentifiers callback called");
//                        if (visitorIDS == null) {
//                            PrefHelper.Debug("visitorIDS = null");
//                            return;
//                        }
//                        int count = 0;
//                        for (VisitorID vid : visitorIDS) {
//                            PrefHelper.Debug(count + " vid = " + vid);
//                            count++;
//                        }
//                    }
//                });
//
//                Identity.getExperienceCloudId(new AdobeCallback<String>() {
//                    @Override
//                    public void call(String visitorID) {
//                        PrefHelper.Debug("getExperienceCloudId, visitorID = " + visitorID);
//                    }
//                });
//            }
//        }, 5000);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
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

    private void initBranchSession() {
        AdobeBranch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                PrefHelper.Debug("JSON: " + referringParams.toString());

                try {
                    // You would think that there was an easier way to figure this out than looking at LinkProperties code
                    if (referringParams.has("+clicked_branch_link") && referringParams.getBoolean("+clicked_branch_link")) {
                        String idString = referringParams.optString(SwagActivity.SWAG_ID);
                        if (idString != null) {
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
        }, getIntent().getData(), this);
    }

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
