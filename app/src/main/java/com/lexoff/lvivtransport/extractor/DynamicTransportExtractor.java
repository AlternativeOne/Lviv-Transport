package com.lexoff.lvivtransport.extractor;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.lexoff.lvivtransport.Client;
import com.lexoff.lvivtransport.info.DynamicRouteInfo;
import com.lexoff.lvivtransport.info.Info;
import com.lexoff.lvivtransport.info.TransportInfo;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;

import okhttp3.Response;

public class DynamicTransportExtractor extends Extractor {
    private String PATH="routes/dynamic/%s";

    public DynamicTransportExtractor(Client client, String routeShortName){
        super(client, "");

        setUrl(String.format(BASE_URL+PATH, routeShortName));
    }

    @Override
    protected Info buildInfo() throws JsonParserException {
        JsonArray jsonArray=JsonParser.array().from(response);

        DynamicRouteInfo info=new DynamicRouteInfo();

        for (Object obj:jsonArray) {
            JsonObject jO=(JsonObject) obj;

            TransportInfo tInfo = new TransportInfo();

            tInfo.setId(jO.getString("id"));
            tInfo.setDirection(jO.getInt("direction"));
            tInfo.setLocation(new GeoPoint(jO.getArray("location").getDouble(0), jO.getArray("location").getDouble(1)));

            tInfo.setBearing(jO.getInt("bearing"));

            info.addTransportInfo(tInfo);
        }

        return info;
    }
}
