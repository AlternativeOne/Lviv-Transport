package com.lexoff.lvivtransport.extractor;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.lexoff.lvivtransport.info.Info;
import com.lexoff.lvivtransport.info.StaticRouteInfo;
import com.lexoff.lvivtransport.info.Stop;
import com.lexoff.lvivtransport.info.TransportTypeEnum;

import org.osmdroid.util.GeoPoint;

public class OfflineStaticRoutesExtractor extends OfflineExtractor {
    private String PATH="routes/%s.json";

    public OfflineStaticRoutesExtractor(String routeShortName){
        super("");

        setUrl(String.format(PATH, routeShortName));
    }

    @Override
    protected Info buildInfo() {
        StaticRouteInfo info=new StaticRouteInfo();

        info.setId(json.getString("id"));
        info.setBackwardColor(json.getString("color"));

        String type=json.getString("type");
        if (type.equals("bus")) {
            info.setType(TransportTypeEnum.BUS);
        } else if (type.equals("tram")){
            info.setType(TransportTypeEnum.TRAM);
        } else if (type.equals("trol")){
            info.setType(TransportTypeEnum.TROL);
        }

        String routeLongName=json.getString("route_long_name");

        info.setRouteShortName(json.getString("route_short_name"));
        info.setRouteLongName(routeLongName);

        int forwardIndex=0, backwardIndex=1;

        JsonArray forwardStops=json.getArray("stops").getArray(forwardIndex);
        for (Object obj : forwardStops){
            JsonObject jO=(JsonObject) obj;

            Stop stop=new Stop();
            stop.setCode(jO.getInt("code"));
            stop.setName(jO.getString("name"));
            stop.setLatitude(jO.getArray("loc").getDouble(0));
            stop.setLongitude(jO.getArray("loc").getDouble(1));

            info.addForwardStop(stop);
        }

        JsonArray backwardStops=json.getArray("stops").getArray(backwardIndex);
        for (Object obj : backwardStops){
            JsonObject jO=(JsonObject) obj;

            Stop stop=new Stop();
            stop.setCode(jO.getInt("code"));
            stop.setName(jO.getString("name"));
            stop.setLatitude(jO.getArray("loc").getDouble(0));
            stop.setLongitude(jO.getArray("loc").getDouble(1));

            info.addBackwardStop(stop);
        }

        JsonArray forwardShapes=json.getArray("shapes").getArray(forwardIndex);
        for (int i=0; i<forwardShapes.size(); i++) {
            JsonArray jArr = forwardShapes.getArray(i);

            info.addForwardShape(new GeoPoint(jArr.getDouble(0), jArr.getDouble(1)));
        }

        JsonArray backwardShapes=json.getArray("shapes").getArray(backwardIndex);
        for (int i=0; i<backwardShapes.size(); i++) {
            JsonArray jArr = backwardShapes.getArray(i);

            info.addBackwardShape(new GeoPoint(jArr.getDouble(0), jArr.getDouble(1)));
        }

        return info;
    }

}
