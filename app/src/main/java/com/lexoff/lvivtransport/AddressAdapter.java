package com.lexoff.lvivtransport;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class AddressAdapter extends BaseAdapter {

    private Context context;
    private List<Address> addresses;

    public AddressAdapter(Context context, List<Address> addresses) {
        this.context = context;
        this.addresses=addresses;
    }

    @Override
    public int getCount() {
        return addresses.size();
    }

    @Override
    public Object getItem(int position) {
        return addresses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.address_list_item, parent, false);
        TextView name_tv = convertView.findViewById(R.id.item_name_tv);
        TextView address_tv = convertView.findViewById(R.id.item_address_tv);

        String a=addresses.get(position).getExtras().getString("display_name");

        name_tv.setText(a.substring(0, a.indexOf(",")).trim());
        address_tv.setText(a.substring(a.indexOf(",")+1).trim());
        return convertView;
    }

}
