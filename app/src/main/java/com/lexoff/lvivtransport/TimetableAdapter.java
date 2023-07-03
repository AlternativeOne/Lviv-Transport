package com.lexoff.lvivtransport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TimetableAdapter extends BaseAdapter {

    private Context context;
    private String[] routes, timeLefts;

    private String localeLangCode;

    public TimetableAdapter(Context context, String[] routes, String[] timeLefts) {
        this.context = context;
        this.routes=routes;
        this.timeLefts=timeLefts;

        this.localeLangCode=Utils.getAppLocaleAsString(context);
    }

    @Override
    public int getCount() {
        return routes.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.timetable_list_item, parent, false);
        TextView route_tv = convertView.findViewById(R.id.item_route_tv);
        TextView timeleft_tv = convertView.findViewById(R.id.item_timeleft_tv);
        route_tv.setText(routes[position]);
        timeleft_tv.setText(Localization.localizeString(timeLefts[position], localeLangCode));
        return convertView;
    }

}
