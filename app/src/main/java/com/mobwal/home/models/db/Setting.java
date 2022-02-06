package com.mobwal.home.models.db;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import com.mobwal.home.R;

public class Setting {
    public Setting() {
        id = UUID.randomUUID().toString();
    }

    public String id;

    public String c_key;

    public String c_value;

    public String f_route;

    public String toKeyName(@NotNull Context context) {
        switch (c_key) {
            case "geo":
                return context.getString(R.string.location);

            case "geo_quality":
                return context.getString(R.string.geo_quality);

            case "image":
                return context.getString(R.string.attach);

            case "image_quality":
                 return context.getString(R.string.image_quality);

            case "image_height":
                 return context.getString(R.string.image_height);

            default:
                return context.getString(R.string.params);
        }
    }
}
