package com.example.filip.myapplication.db;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by Filip on 2014-10-12 012.
 */
public class JSONParser {
    public JSONObject getJSON(String url, List<NameValuePair> params) {

        String json = null;
        JSONObject jsonObject = null;

        // Send POST to url, get String response
        // TODO consider changing DefaultHttpClient to HttpURLConnection
        // TODO consider not getting response from Http
        try {
            DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

            Log.d("JSONParser", "httpPost is: " + EntityUtils.toString(httpPost.getEntity()));

            HttpResponse httpResponse = defaultHttpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            json = EntityUtils.toString(httpEntity);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("JSONParser", "String is: " + json);

        // Turn String response into JSONObject
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            //e.printStackTrace();
            Log.e("JSONParser", "Error parsing String to JSON: " + e.toString());
        }

        return jsonObject;
    }
}
