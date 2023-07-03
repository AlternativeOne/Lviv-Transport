package com.lexoff.lvivtransport.extractor;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.lexoff.lvivtransport.Client;
import com.lexoff.lvivtransport.info.ClosestTransportInfo;
import com.lexoff.lvivtransport.info.ClosestTransportsInfo;
import com.lexoff.lvivtransport.info.Info;
import com.lexoff.lvivtransport.info.TransportTypeEnum;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;

import okhttp3.Response;

public class ClosestTransportExtractor extends Extractor {
    private String PATH="transport?longitude=%f&latitude=%f";

    public ClosestTransportExtractor(Client client, double latitude, double longitude){
        super(client, "");

        setUrl(String.format(BASE_URL+PATH, longitude, latitude).replaceAll(",", "."));
    }

    @Override
    protected Info buildInfo() throws JsonParserException {
        JsonArray jsonArray=JsonParser.array().from(response);

        ClosestTransportsInfo info=new ClosestTransportsInfo();

        for (Object obj : jsonArray){
            JsonObject jO=(JsonObject) obj;

            ClosestTransportInfo tInfo=new ClosestTransportInfo();
            tInfo.setId(jO.getString("id"));
            tInfo.setColor(jO.getString("color"));
            tInfo.setShortRouteName(jO.getString("route"));

            String type=jO.getString("vehicle_type");
            if (type.equals("bus")) {
                tInfo.setType(TransportTypeEnum.BUS);
            } else if (type.equals("tram")){
                tInfo.setType(TransportTypeEnum.TRAM);
            } else if (type.equals("trol")){
                tInfo.setType(TransportTypeEnum.TROL);
            }

            tInfo.setLocation(new GeoPoint(jO.getArray("location").getDouble(0), jO.getArray("location").getDouble(1)));

            tInfo.setBearing(jO.getInt("bearing"));

            info.addTransportInfo(tInfo);
        }

        return info;
    }
}
