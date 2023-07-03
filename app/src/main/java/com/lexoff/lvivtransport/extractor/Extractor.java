package com.lexoff.lvivtransport.extractor;

import com.grack.nanojson.JsonParserException;
import com.lexoff.lvivtransport.Client;
import com.lexoff.lvivtransport.exception.ServiceUnavailableException;
import com.lexoff.lvivtransport.info.Info;

import java.io.IOException;

import okhttp3.Response;

public abstract class Extractor {
    protected String BASE_URL="https://api.lad.lviv.ua/";

    protected String url;
    protected String data;

    private int method=0;
    private boolean throwOnErrorCodes=true;

    protected Client client;
    protected boolean pageFetched = false;

    protected String response;

    protected Extractor(Client client, String url){
        this.client=client;
        this.url=url;
    }

    public void setUrl(String url){
        this.url=url;
    }

    public void setData(String data){
        this.data=data;
    }

    public void setGET(){
        method=0;
    }

    public void setPOST(){
        method=1;
    }

    public void setThrowOnErrorCodes(boolean throwOnErrorCodes){
        this.throwOnErrorCodes=throwOnErrorCodes;
    }

    public void fetchPage() throws IOException, ServiceUnavailableException, JsonParserException {
        if (pageFetched) return;
        Response response=null;
        if (method==0) {
            response=client.get(url);
        } else if (method==1){
            response=client.post(url, data);
        }

        if (throwOnErrorCodes && (response==null || response.code()!=200)){
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

    public void onPageFetched(Response response) throws IOException, JsonParserException {
        String body=response.body().string();

        this.response=body;
    }

    public Info getInfo() throws IOException, ServiceUnavailableException, JsonParserException {
        fetchPage();

        return buildInfo();
    }

    protected abstract Info buildInfo() throws JsonParserException;
}
