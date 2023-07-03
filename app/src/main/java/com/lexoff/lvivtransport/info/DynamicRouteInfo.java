package com.lexoff.lvivtransport.info;

import java.util.ArrayList;
import java.util.List;

public class DynamicRouteInfo extends Info {
    private List<TransportInfo> transportInfos;

    public DynamicRouteInfo(){
        transportInfos=new ArrayList<>();
    }

    public void addTransportInfo(TransportInfo info){
        transportInfos.add(info);
    }

    public List<TransportInfo> getTransportInfos() {
        return transportInfos;
    }

    public int countTransport(){
        return transportInfos.size();
    }
}
