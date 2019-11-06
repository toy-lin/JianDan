package com.socks.jiandan.net;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.reflect.TypeToken;
import com.socks.jiandan.model.CasualPhoto;

public class Request4ImageDetail extends Request<CasualPhoto> {

    private Response.Listener<CasualPhoto> listener;

    public Request4ImageDetail(String url, Response.Listener<CasualPhoto> listener,
                               Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.listener = listener;
    }

    @Override
    protected Response<CasualPhoto> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonStr = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            CasualPhoto pictures = (CasualPhoto) JSONParser.toObject(jsonStr,
                    new TypeToken<CasualPhoto>() {
                    }.getType());
            return Response.success(pictures, HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void deliverResponse(CasualPhoto response) {
        listener.onResponse(response);
    }
}
