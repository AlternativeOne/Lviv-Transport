package com.lexoff.lvivtransport.info;

import java.util.ArrayList;
import java.util.List;

public class StopsInfo extends Info {
    private List<Stop> stops;

    public StopsInfo(){
        stops=new ArrayList<>();
    }

    public void addStop(Stop stop){
        stops.add(stop);
    }

    public List<Stop> getStops() {
        return stops;
    }
}
