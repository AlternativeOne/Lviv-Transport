package com.lexoff.lvivtransport.extractor;

import com.grack.nanojson.JsonParserException;
import com.lexoff.lvivtransport.info.Info;
import com.lexoff.lvivtransport.info.Stop;
import com.lexoff.lvivtransport.info.StopsInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class OfflineClosestStopsExtractor extends OfflineExtractor {
    private String PATH="stops.html";

    private String body;

    public OfflineClosestStopsExtractor(){
        super("");

        setUrl(PATH);
    }

    @Override
    public void onPageFetched(String response) throws IOException, JsonParserException {
        body = response;
    }

    @Override
    protected Info buildInfo() {
        StopsInfo info=new StopsInfo();

        Document doc=Jsoup.parse(body);

        Elements trs=doc.select("tr");
        for (Element tr : trs){
            Stop item=new Stop();

            Elements tds=tr.select("td");
            if (tds.size()==0) continue;

            item.setCode(Integer.parseInt(tds.get(0).text().trim()));
            item.setName(tds.get(2).text().trim());

            String position=tds.get(3).text().trim();
            item.setLatitude(Double.parseDouble(position.substring(0, position.indexOf(",")).trim()));
            item.setLongitude(Double.parseDouble(position.substring(position.indexOf(",")+1).trim()));

            item.addRoutesAvailable(tds.get(4).text().trim().split(" "));

            info.addStop(item);
        }

        return info;
    }
}
