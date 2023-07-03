package com.lexoff.lvivtransport.extractor;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.lexoff.lvivtransport.App;
import com.lexoff.lvivtransport.Client;
import com.lexoff.lvivtransport.info.Info;
import com.lexoff.lvivtransport.exception.ServiceUnavailableException;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;

public abstract class OfflineExtractor extends Extractor {
    protected String url;
    protected boolean pageFetched = false;

    protected JsonObject json;

    protected OfflineExtractor(String url){
        super(null, url);

        this.url=url;
    }

    public void setUrl(String url){
        this.url=url;
    }

    public void fetchPage() throws IOException, ServiceUnavailableException, JsonParserException {
        if (pageFetched) return;

        String response="";

        try {
            InputStream stream = App.getApp().getAssets().open(url);

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            response=new String(buffer);
        } catch (IOException e) {
            throw new ServiceUnavailableException("");
        }

        onPageFetched(response);
        pageFetched = true;
    }

    protected void assertPageFetched() {
        if (!pageFetched) throw new IllegalStateException("Page is not fetched. Make sure you call fetchPage()");
    }

    protected boolean isPageFetched() {
        return pageFetched;
    }

    public void onPageFetched(String response) throws IOException, JsonParserException {
        json=JsonParser.object().from(response);
    }

    protected JsonObject getJsonResponse(){
        assertPageFetched();

        return json;
    }

    public Info getInfo() throws JsonParserException, IOException, ServiceUnavailableException {
        fetchPage();

        return buildInfo();
    }

    protected abstract Info buildInfo();
}
