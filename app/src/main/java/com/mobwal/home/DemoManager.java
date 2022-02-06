package com.mobwal.home;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import com.mobwal.home.models.DemoPlaceItem;
import com.mobwal.home.utilits.NetworkInfoUtil;

public class DemoManager {
    public static final String API_KEY="5ae2e3f221c38a28845f05b603077038a9060a9b3e50cbbe0403d96b";

    /**
     * Получение строки для запроса
     * @param lang язык интерфейса
     * @param radius радиус получения данных
     * @param lon долгота
     * @param lat широта
     * @return адрес запроса
     * @throws MalformedURLException исключение
     */
    public static URL getUrl(String lang, int radius, double lat, double lon) throws MalformedURLException {
        return new URL("https://api.opentripmap.com/0.1/" + lang + "/places/radius?radius=" + radius + "&lon=" + lon + "&lat=" + lat + "&rate=3&format=geojson&apikey=" + API_KEY);
    }

    /**
     * Список объектов после запроса
     * @param url адрес запроса
     * @return список данных
     * @throws IOException исключение
     */
    @NotNull
    public static DemoPlaceItem[] getObjects(@NotNull URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        List<DemoPlaceItem> items = new ArrayList<>();
        try {
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(3000);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Scanner s = new Scanner(in).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";
            try {
                JSONObject object = new JSONObject(response);
                JSONArray array = object.getJSONArray("features");

                for(int i = 0;i < array.length(); i++){
                    JSONObject obj = array.getJSONObject(i);
                    DemoPlaceItem item = new DemoPlaceItem(obj);
                    if(!TextUtils.isEmpty(item.name)) {
                        items.add(item);
                    }
                }
            } catch (Exception e) {
                WalkerApplication.Log("Demo. Ошибка чтения результата достопримечательностей.", e);
            }
        } catch (Exception e) {
            WalkerApplication.Log("Demo. Ошибка запроса на получение достопримечательностей.", e);
        } finally {
            urlConnection.disconnect();
        }

        return items.toArray(new DemoPlaceItem[0]);
    }
}
