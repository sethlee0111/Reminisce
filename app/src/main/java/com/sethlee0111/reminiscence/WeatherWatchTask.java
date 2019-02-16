package com.sethlee0111.reminiscence;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherWatchTask extends AsyncTask<Double, Void, String> {

    private Exception exception;
    private final String API_KEY = "db50775d734228c63c41d368a51a1cec";

    protected void onPreExecute() {
//        progressBar.setVisibility(View.VISIBLE);
//        responseView.setText("");
    }

    protected String doInBackground(Double... loc) {
        try {
            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?" + "lat=" + loc[0] + "&lon=" + loc[1] + "&apiKey=" + API_KEY);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                String res = stringBuilder.toString();
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonTree = jsonParser.parse(res);
                Log.d("JsonParse", jsonTree.toString());

                if(jsonTree.isJsonObject()) {
                    JsonObject jsonObject = jsonTree.getAsJsonObject();
                    JsonElement e = jsonObject.get("weather");
                    Log.d("JsonParse", e.toString());
                    JsonArray ja = e.getAsJsonArray();
                    if(ja.get(0).isJsonObject()) {
                        JsonObject jobj = ja.get(0).getAsJsonObject();
                        JsonElement e1 = jobj.get("main");
                        Log.d("JsonParse", e1.toString());
                        return e1.getAsString();
                    }
                }
                else
                    return "Unknown";
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
        return null;
    }

    protected void onPostExecute(String response) {
        if(response == null) {
            response = "THERE WAS AN ERROR";
        }
//        progressBar.setVisibility(View.GONE);
        Log.i("INFO", response);
//        responseView.setText(response);
    }
}