package com.mobwal.home.models;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mobwal.home.R;

public class DemoPlaceItem {
    public String name;
    public String kinds;
    public Double latitude;
    public Double longitude;

    public DemoPlaceItem(@NotNull JSONObject obj) throws JSONException {
        JSONObject prop = obj.getJSONObject("properties");
        if(!prop.getString("name").isEmpty()){
            name = prop.getString("name");
            kinds = prop.getString("kinds");
            JSONObject geo = obj.getJSONObject("geometry");
            JSONArray coordinates = geo.getJSONArray("coordinates");
            longitude= coordinates.getDouble(0);
            latitude = coordinates.getDouble(1);
        }
    }
}
