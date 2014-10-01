package com.cartalk.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;
 

public class HttpUtil {
 
	private static final String TAG = "HttpUtil";
    public static String http(String url, String method,Map<String, String> params) {
        URL u = null;
        HttpURLConnection con = null;

        StringBuffer sb = new StringBuffer();
        StringBuffer buffer = new StringBuffer();
        if(params!=null){
            for (Entry<String, String> e : params.entrySet()) {
                sb.append(e.getKey());
                sb.append("=");
                sb.append(e.getValue());
                sb.append("&");
            }
            sb.substring(0, sb.length() - 1);
        }

        try {
        	Log.d(TAG, "upload data url:"+url);
            u = new URL(url);
            con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod(method);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setConnectTimeout(5000);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            Log.d(TAG, "upload data:"+sb.toString());
            osw.write(sb.toString());
            osw.flush();
            osw.close();
            int code=con.getResponseCode();
            Log.d(TAG, "upload code:"+code);
            if(code==200){
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                String temp;
                while ((temp = br.readLine()) != null) {
                    buffer.append(temp);
                    buffer.append("\n");
                }
            }
       } catch (Exception e) {
           e.printStackTrace();
           Log.e(TAG, e.getMessage());
       } finally {
           if (con != null) {
               con.disconnect();
           }
       }
       return buffer.toString();
    }
}
