package com.lexoff.lvivtransport.extractor;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.lexoff.lvivtransport.info.Info;
import com.lexoff.lvivtransport.info.Stop;
import com.lexoff.lvivtransport.info.TransferInfo;
import com.lexoff.lvivtransport.info.TransfersInfo;
import com.lexoff.lvivtransport.info.TransportTypeEnum;

public class OfflineStopStaticExtractor extends OfflineExtractor {
    private String PATH="stops/%d.json";

    public OfflineStopStaticExtractor(int code){
        super("");

        setUrl(String.format(PATH, code));
    }

    @Override
    protected Info buildInfo() {
        Stop info=new Stop();

        info.setCode(json.getInt("code"));
        info.setName(json.getString("name"));
        info.setLatitude(json.getDouble("latitude"));
        info.setLongitude(json.getDouble("longitude"));

        TransfersInfo transfersInfo=new TransfersInfo();

        JsonArray routes=json.getArray("transfers");
        for (Object obj : routes){
            JsonObject jO=(JsonObject) obj;

            info.addRoutesAvailable(jO.getString("route").replace("А", "A").replace("Т", "T"));

            TransferInfo tInfo=new TransferInfo();
            tInfo.setId(jO.getString("id"));
            tInfo.setColor(jO.getString("color"));
            tInfo.setRouteShortName(jO.getString("route").replace("А", "A").replace("Т", "T"));

            String type=jO.getString("vehicle_type");
            if (type.equals("bus")) {
                tInfo.setVehicleType(TransportTypeEnum.BUS);
            } else if (type.equals("tram")){
                tInfo.setVehicleType(TransportTypeEnum.TRAM);
            } else if (type.equals("trol")){
                tInfo.setVehicleType(TransportTypeEnum.TROL);
            }

            tInfo.setDirection(jO.getInt("direction_id"));

            transfersInfo.addTransferInfo(tInfo);
        }

        info.setTransfersInfo(transfersInfo);

        return info;
    }
}
