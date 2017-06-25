package xyz.agetty.rssfeedreader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Royston on 6/9/2017.
 */

public class FeedsListViewAdapter extends ArrayAdapter<FeedsListViewDataModel> implements View.OnClickListener{

    private ArrayList<FeedsListViewDataModel> dataSet;
    Context mContext;
    MainActivity main;

    private static class ViewHolder {
        TextView txtName;
        TextView txtURL;
    }

    public FeedsListViewAdapter(MainActivity main, ArrayList<FeedsListViewDataModel> data, Context context) {
        super(context, R.layout.feedslistviewrow, data);
        this.dataSet = data;
        this.mContext = context;
        this.main = main;
    }

    @Override
    public void onClick(View v) {

        int position = (Integer) v.getTag();
        Object object = getItem(position);
        FeedsListViewDataModel dataModel = (FeedsListViewDataModel)object;
        System.out.println("Click " + v.getId() + " pos " +  position);
        switch (v.getId())
        {

            case R.id.feedlistitem:

                break;
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FeedsListViewDataModel dataModel = getItem(position);

        ViewHolder viewHolder;

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.feedslistviewrow, parent, false);

            viewHolder.txtName = (TextView) convertView.findViewById(R.id.name);
            viewHolder.txtURL = (TextView) convertView.findViewById(R.id.url);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }


        viewHolder.txtName.setText(dataModel.getName());
        viewHolder.txtURL.setText(dataModel.getURL());
        viewHolder.txtURL.setTextColor(Color.parseColor("#808080"));


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String feedName = ((TextView)view.findViewById(R.id.name)).getText().toString();
                main.readFeed(feedName);

            }
        });


        return convertView;
    }
}