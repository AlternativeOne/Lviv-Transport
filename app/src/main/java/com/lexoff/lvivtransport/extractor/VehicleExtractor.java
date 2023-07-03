package com.lexoff.lvivtransport.extractor;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.lexoff.lvivtransport.Client;
import com.lexoff.lvivtransport.info.Info;
import com.lexoff.lvivtransport.info.TransportInfo;

import org.osmdroid.util.GeoPoint;

public class VehicleExtractor extends Extractor {
    private String PATH="vehicle/%s";

    public VehicleExtractor(Client client, String code){
        super(client, "");

        setUrl(String.format(BASE_URL+PATH, code));
    }

    @Override
    protected Info buildInfo() throws JsonParserException {
        JsonObject json=JsonParser.object().from(response);

        TransportInfo info=new TransportInfo();

        String splits[]=url.split("/");
        info.setId(splits[splits.length-1]);
        info.setShortRouteName(json.getString("routeId"));
        info.setDirection(json.getInt("direction"));
        info.setLocation(
                new GeoPoint(json.getArray("location").getDouble(0),
                        json.getArray("location").getDouble(1))
        );

        return info;
    }
}
