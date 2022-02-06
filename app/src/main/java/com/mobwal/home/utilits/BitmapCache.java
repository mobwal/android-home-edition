package com.mobwal.home.utilits;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.mobwal.home.Names;
import com.mobwal.home.WalkerApplication;

public class BitmapCache extends LruCache<String, Bitmap> {
    private static final int MAXIMUM_SIZE_IN_KB = 1024 * 16;

    public BitmapCache() {
        super(getCacheSize());
    }

    @Override
    protected int sizeOf(@NonNull String key, @NonNull Bitmap value) {
        return value.getByteCount() / 1024;
    }

    private static int getCacheSize() {
        final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = Math.min(maxMemory / 8, MAXIMUM_SIZE_IN_KB);
        WalkerApplication.Debug("BitmapCache size: " + cacheSize + "kb");
        return cacheSize;
    }

    /**
     * Получение изображения
     * @param key ключ
     * @param bytes массив байтов
     * @param desiredWidth предполагаемый размер
     * @return изображение
     */
    public synchronized static Bitmap getBitmap(@NonNull String key, @NonNull byte[] bytes, int desiredWidth) {
        Bitmap cache = WalkerApplication.getBitmap(key);
        if(cache == null) {
            Bitmap bitmap = ImageUtil.getSizedBitmap(bytes, 0, bytes.length, desiredWidth);
            WalkerApplication.cacheBitmap(key, bitmap);
            return bitmap;
        } else {
            return cache;
        }
    }
}
