package com.lexoff.lvivtransport.extractor;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.lexoff.lvivtransport.Client;
import com.lexoff.lvivtransport.info.Info;
import com.lexoff.lvivtransport.info.Stop;
import com.lexoff.lvivtransport.info.StopsInfo;

import java.io.IOException;

import okhttp3.Response;

public class ClosestStopsExtractor extends Extractor {
    private String PATH="closest?longitude=%f&latitude=%f";

    public ClosestStopsExtractor(Client client, double latitude, double longitude){
        super(client, "");

        setUrl(String.format(BASE_URL+PATH, longitude, latitude).replaceAll(",", "."));
    }

    @Override
    protected Info buildInfo() throws JsonParserException {
        JsonArray jsonArray=JsonParser.array().from(response);

        StopsInfo stopsInfo=new StopsInfo();

        for (Object obj : jsonArray){
            JsonObject jO=(JsonObject) obj;

            Stop stop=new Stop();
            stop.setCode(jO.getInt("code"));
            stop.setName(jO.getString("name"));
            stop.setLongitude(jO.getDouble("longitude"));
            stop.setLatitude(jO.getDouble("latitude"));

            stopsInfo.addStop(stop);
        }

        return stopsInfo;
    }
}
