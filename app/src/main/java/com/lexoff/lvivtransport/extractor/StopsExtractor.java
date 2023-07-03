package com.lexoff.lvivtransport.extractor;

import com.grack.nanojson.JsonParserException;
import com.lexoff.lvivtransport.Client;
import com.lexoff.lvivtransport.info.Info;
import com.lexoff.lvivtransport.info.Stop;
import com.lexoff.lvivtransport.info.StopsInfo;

import java.io.IOException;

import okhttp3.Response;

public class StopsExtractor extends Extractor {
    private String PATH="stops";

    public StopsExtractor(Client client){
        super(client, "");

        setUrl(BASE_URL+PATH);
    }

    @Override
    protected Info buildInfo() {
        StopsInfo info=new StopsInfo();

        String splits[]=response.split("<td>");

        Stop stop=new Stop();

        int a=0;
        for (int i=1; i<splits.length; i++){
            if (a==5) {
                info.addStop(stop);
                stop=new Stop();

                a=0;
            }

            String s=splits[i];

            if (a==0){
                int i1=s.indexOf("</a></td>");
                int i2=s.indexOf(">", i1-17);

                int code=Integer.parseInt(s.substring(i2+1, i1));
                stop.setCode(code);
            } else if (a==2){
                int i1=s.indexOf("</td>");

                stop.setName(s.substring(0, i1));
            } else if (a==3){
                int i1=s.indexOf("</a></td>");
                int i2=s.indexOf(">", i1-30);
                String s2=s.substring(i2+1, i1);

                int i3=s2.indexOf(",");
                stop.setLatitude(Double.parseDouble(s2.substring(0, i3)));
                stop.setLongitude(Double.parseDouble(s2.substring(i3+1)));
            } else if (a==4){
                int i1=s.indexOf("</td>");

                stop.addRoutesAvailable(s.substring(0, i1).split(" "));
            }

            a++;
        }

        return info;
    }

}
