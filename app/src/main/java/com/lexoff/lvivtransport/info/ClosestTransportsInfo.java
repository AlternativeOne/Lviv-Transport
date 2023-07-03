package com.lexoff.lvivtransport.info;

import java.util.ArrayList;
import java.util.List;

public class ClosestTransportsInfo extends Info {
    private List<ClosestTransportInfo> transportsInfos;

    public ClosestTransportsInfo(){
        transportsInfos=new ArrayList<>();
    }


    public List<ClosestTransportInfo> getTransportsInfos() {
        return transportsInfos;
    }

    public void addTransportInfo(ClosestTransportInfo transportInfo) {
        transportsInfos.add(transportInfo);
    }
}
