package com.lexoff.lvivtransport.extractor;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.lexoff.lvivtransport.Client;
import com.lexoff.lvivtransport.info.Info;
import com.lexoff.lvivtransport.info.RailroadStationInfo;
import com.lexoff.lvivtransport.info.RailroadStationsInfo;

import java.util.Objects;

public class RailroadStationsExtractor extends Extractor {
    private String PATH="https://overpass-api.de/api/interpreter";
    private String data="data=%5Bout%3Ajson%5D%5Btimeout%3A25%5D%3B%0A(%0A++node%5B%22railway%22%3D%22station%22%5D(49.775278165136655%2C23.845138549804688%2C49.89662485669814%2C24.19361114501953)%3B%0A++way%5B%22railway%22%3D%22station%22%5D(49.775278165136655%2C23.845138549804688%2C49.89662485669814%2C24.19361114501953)%3B%0A++relation%5B%22railway%22%3D%22station%22%5D(49.775278165136655%2C23.845138549804688%2C49.89662485669814%2C24.19361114501953)%3B%0A)%3B%0Aout+body%3B%0A%3E%3B%0Aout+skel+qt%3B";

    public RailroadStationsExtractor(Client client){
        super(client, "");

        setUrl(PATH);
        setData(data);
        setPOST();
    }

    @Override
    protected Info buildInfo() throws JsonParserException {
        JsonObject json=JsonParser.object().from(response);

        RailroadStationsInfo info=new RailroadStationsInfo();

        json.getArray("elements")
                .stream()
                .filter(Objects::nonNull)
                .map(JsonObject.class::cast)
                .filter(obj -> "node".equals(obj.getString("type")))
                .forEach(obj -> {
                    RailroadStationInfo item=new RailroadStationInfo();

                    item.setLatitutde(obj.getDouble("lat"));
                    item.setLongitude(obj.getDouble("lon"));

                    String name=obj.getObject("tags").getString("name:uk");
                    if (name==null || name.isEmpty()){
                        name=obj.getObject("tags").getString("name");
                    }
                    item.setName(name);

                    info.addItem(item);
                });

        return info;
    }
}
