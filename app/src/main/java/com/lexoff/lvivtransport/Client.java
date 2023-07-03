package com.lexoff.lvivtransport;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Client {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0";

    private static Client instance;
    private OkHttpClient client;

    private Client(OkHttpClient.Builder builder) {
        OkHttpClient.Builder b = builder
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS);

        this.client = b.build();
    }

    public static Client init(@Nullable OkHttpClient.Builder builder) {
        return instance = new Client(builder != null ? builder : new OkHttpClient.Builder());
    }

    public static Client getInstance() {
        return instance;
    }

    public Response get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", USER_AGENT)
                .method("GET", null)
                .build();

        Response response = client.newCall(request).execute();

        return response;
    }

    public Response post(String url, String data) throws IOException {
        RequestBody body = RequestBody.create(null, data.getBytes());

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", USER_AGENT)
                .method("POST", body)
                .build();

        Response response = client.newCall(request).execute();

        return response;
    }

}
