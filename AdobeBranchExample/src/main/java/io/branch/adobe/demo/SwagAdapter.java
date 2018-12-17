package io.branch.adobe.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.branch.adobe.demo.model.SwagModel;

public class SwagAdapter extends ArrayAdapter<SwagModel> {

    private int resourceLayout;

    public SwagAdapter(Context context, int resource, List<SwagModel> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(resourceLayout, null);
        }

        SwagModel model = getItem(position);

        if (model != null) {
            ImageView image = v.findViewById(R.id.content_img);
            TextView title = v.findViewById(R.id.title_txt);
            TextView description = v.findViewById(R.id.description_txt);

            if (image != null) {
                image.setImageResource(findImageResource(model.getId()));
            }

            if (title != null) {
                title.setText(model.getTitle());
            }

            if (description != null) {
                description.setText(model.getDescription());
            }
        }

        return v;
    }

    static int findImageResource(int swagId) {
        // This is a little contrived for demo purposes
        int id = android.R.drawable.ic_dialog_alert;

        switch(swagId) {
            case 1:
                // Glasses
                id = R.mipmap.glasses;
                break;

            case 2:
                // Stickers
                id = R.mipmap.stickers;
                break;
        }

        return id;
    }
}
