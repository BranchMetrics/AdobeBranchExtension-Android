package io.branch.adobe.demo;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import io.branch.adobe.demo.model.SwagModel;

public class SwagActivity extends AppCompatActivity {
    public static final String SWAG_DATA = "swag";
    public static final String EXTRA_USERACTION = "extra_user";

    private SwagModel mSwagModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swag);

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String swagData  = extras.getString(SWAG_DATA, null);
            if (swagData != null) {
                init(swagData);
            }
        }
    }

    private void init(String swagData) {
        try {
            JSONObject jsonObject = new JSONObject(swagData);
            mSwagModel = new SwagModel(jsonObject);
        } catch (JSONException e) {
        }

        if (mSwagModel != null) {
            ImageView image = (ImageView) findViewById(R.id.content_img);
            TextView title = (TextView) findViewById(R.id.title_txt);
            TextView description = (TextView) findViewById(R.id.description_txt);

            if (image != null) {
                image.setImageResource(SwagAdapter.findImageResource(mSwagModel.getId()));
            }
            if (title != null) {
                title.setText(mSwagModel.getTitle());
            }

            if (description != null) {
                description.setText(mSwagModel.getDescription());
            }
        }
    }
}
