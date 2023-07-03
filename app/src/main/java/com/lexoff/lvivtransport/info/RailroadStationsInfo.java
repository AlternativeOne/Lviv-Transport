package com.lexoff.lvivtransport.info;

import java.util.ArrayList;
import java.util.List;

public class RailroadStationsInfo extends Info {

    private ArrayList<RailroadStationInfo> items;

    public RailroadStationsInfo(){
        items=new ArrayList<>();
    }

    public List<RailroadStationInfo> getItems(){
        return items;
    }

    public void addItem(RailroadStationInfo item){
        items.add(item);
    }

}
