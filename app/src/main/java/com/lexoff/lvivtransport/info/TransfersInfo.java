package com.lexoff.lvivtransport.info;

import java.util.ArrayList;
import java.util.List;

public class TransfersInfo extends Info {
    private List<TransferInfo> transfers;

    public TransfersInfo(){
        transfers=new ArrayList<>();
    }

    public void addTransferInfo(TransferInfo transfer){
        transfers.add(transfer);
    }

    public List<TransferInfo> getTransfers(){
        return transfers;
    }

}
