package com.lexoff.lvivtransport.extractor;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.lexoff.lvivtransport.Client;
import com.lexoff.lvivtransport.info.Info;
import com.lexoff.lvivtransport.info.Stop;
import com.lexoff.lvivtransport.info.TransferInfo;
import com.lexoff.lvivtransport.info.TransfersInfo;
import com.lexoff.lvivtransport.info.TransportTypeEnum;

public class StopStaticExtractor extends Extractor {
    private String PATH="stops/%d/static";

    public StopStaticExtractor(Client client, int code){
        super(client, "");

        setUrl(String.format(BASE_URL+PATH, code));
    }

    @Override
    protected Info buildInfo() throws JsonParserException {
        JsonObject json=JsonParser.object().from(response);

        Stop info=new Stop();

        info.setCode(json.getInt("code"));
        info.setName(json.getString("name"));
        info.setLatitude(json.getDouble("latitude"));
        info.setLongitude(json.getDouble("longitude"));

        TransfersInfo transfersInfo=new TransfersInfo();

        JsonArray routes=json.getArray("transfers");
        for (Object obj : routes){
            JsonObject jO=(JsonObject) obj;

            info.addRoutesAvailable(jO.getString("route"));

            TransferInfo tInfo=new TransferInfo();
            tInfo.setId(jO.getString("id"));
            tInfo.setColor(jO.getString("color"));
            tInfo.setRouteShortName(jO.getString("route"));

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
