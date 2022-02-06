package com.mobwal.home.models;

import java.util.Hashtable;
import java.util.Map;

/**
 * Инерпретация настроек маршрута
 */
public class SettingRoute {

    public SettingRoute(Hashtable<String, String> hashtable) {
        for (Map.Entry<String, String> item: hashtable.entrySet()) {
            switch (item.getKey()) {
                case "geo":
                    geo = item.getValue().equals("true");
                    break;
                case "geo_quality":
                    geo_quality = item.getValue();
                    break;
                case "image":
                    image = item.getValue().equals("true");
                    break;
                case "image_quality":
                    try {
                        image_quality = Double.parseDouble(item.getValue());
                    } catch (NumberFormatException ignored) {
                        image_quality = 0.6;
                    }
                    break;
                case "image_height":
                    try {
                        image_height = Integer.parseInt(item.getValue());
                    }catch (NumberFormatException ignored) {
                        image_height = 720;
                    }
                    break;
            }
        }
    }

    // обязательность геолокации
    public boolean geo = false;
    // точность геолокации
    public String geo_quality = "LOW";
    // обязательность изображения
    public boolean image = false;
    // качество изображения
    public double image_quality = 0.6;
    // высота изображения
    public int image_height = 720;
}
