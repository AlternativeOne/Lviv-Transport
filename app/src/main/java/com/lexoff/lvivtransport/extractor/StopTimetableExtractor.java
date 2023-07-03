package com.lexoff.lvivtransport.extractor;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.lexoff.lvivtransport.Client;
import com.lexoff.lvivtransport.info.Info;
import com.lexoff.lvivtransport.info.StopTimetable;
import com.lexoff.lvivtransport.info.StopTimetablesInfo;
import com.lexoff.lvivtransport.info.TransportInfo;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;

import okhttp3.Response;

public class StopTimetableExtractor extends Extractor {
    private String PATH="stops/%d/timetable";

    public StopTimetableExtractor(Client client, int code){
        super(client, "");

        setUrl(String.format(BASE_URL+PATH, code));
    }

    @Override
    protected Info buildInfo() throws JsonParserException {
        JsonArray jsonArray= JsonParser.array().from(response);

        StopTimetablesInfo info=new StopTimetablesInfo();

        for (Object obj : jsonArray){
            JsonObject jO=(JsonObject) obj;

            StopTimetable stopTimetable=new StopTimetable();
            stopTimetable.setRouteId(jO.getString("route_id"));
            stopTimetable.setArrivalTime(jO.getString("arrival_time"));
            stopTimetable.setTimeLeft(jO.getString("time_left"));

            TransportInfo tInfo=new TransportInfo();
            tInfo.setId(jO.getString("vehicle_id"));
            tInfo.setLocation(
                    new GeoPoint(Double.parseDouble(jO.getArray("location").getString(0)),
                                 Double.parseDouble(jO.getArray("location").getString(1)))
            );
            tInfo.setDirection(jO.getInt("direction_id"));
            tInfo.setShortRouteName(jO.getString("route"));

            tInfo.setBearing(jO.getInt("bearing"));

            stopTimetable.setTransportInfo(tInfo);
            info.addTimetable(stopTimetable);
        }

        return info;
    }
}
