package com.mobwal.home;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mobwal.home.utilits.BitmapCache;

public class WalkerApplication extends Application {

    private boolean isAuthorized = false;
    private static boolean ReportSending = false;
    public static boolean Debug = false;

    private static final BitmapCache sBitmapCache = new BitmapCache();

    private WalkerSQLContext mWalkerSQLContext;

    /**
     * Сохранение данных в кэше
     * @param key ключ
     * @param bitmap изображение
     */
    public synchronized static void cacheBitmap(@NotNull String key, @NotNull Bitmap bitmap) {
        sBitmapCache.put(key, bitmap);
    }

    /**
     * Получение данных из кэш
     * @param key ключ
     * @return изображение
     */
    @Nullable
    public synchronized static Bitmap getBitmap(@NotNull String key) {
        return sBitmapCache.get(key);
    }

    /**
     * Установка призначка авторизации в приложении
     * @param context контекст
     * @param authorized признак авторизации
     */
    public static void setAuthorized(Context context, boolean authorized) {
        WalkerApplication app = (WalkerApplication)context.getApplicationContext();
        app.isAuthorized = authorized;
    }

    /**
     * Получение признака авторизации
     * @param context текущий контекст
     * @return возвращается признак авторизации
     */
    public static boolean getAuthorized(Context context) {
        WalkerApplication app = (WalkerApplication)context.getApplicationContext();
        return app.isAuthorized;
    }

    /**
     * Подключение к БД
     * @param context контекст
     * @return подключение
     */
    public static WalkerSQLContext getWalkerSQLContext(Context context) {
        WalkerApplication app = (WalkerApplication)context.getApplicationContext();
        return app.mWalkerSQLContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWalkerSQLContext = new WalkerSQLContext(this);

        SharedPreferences sharedPreferences = getSharedPreferences(Names.PREFERENCE_NAME, MODE_PRIVATE);

        Debug = sharedPreferences.getBoolean("debug", false);
        ReportSending = sharedPreferences.getBoolean("error_reporting", false);

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(ReportSending);

        if(ReportSending) {
            FirebaseCrashlytics.getInstance().setCustomKey("debug", Debug);
            FirebaseCrashlytics.getInstance().setCustomKey("pin_use", !sharedPreferences.getString("pin_code", "").isEmpty());
        }
    }

    /**
     * Логирование действий пользователя
     * @param message сообщение
     */
    public static void Log(String message) {
        Log(message, null);
    }

    /**
     * Логирование действий пользователя
     * @param message сообщение
     * @param exception исключение
     */
    public static void Log(String message, @Nullable Exception exception) {
        if(ReportSending) {
            if(exception != null) {
                FirebaseCrashlytics.getInstance().recordException(exception);
            }
            FirebaseCrashlytics.getInstance().log(message);
        }
    }

    /**
     * Логирование действий пользователя в режиме отладки
     * @param message сообщение
     */
    public static void Debug(@NotNull String message) {
        Debug(message, null);
    }

    /**
     * Логирование действий пользователя в режиме отладки
     * @param message сообщение
     * @param exception исключение
     */
    public static void Debug(@NotNull String message, @Nullable Exception exception) {
        if(Debug && ReportSending) {
            if(exception != null) {
                FirebaseCrashlytics.getInstance().recordException(exception);
            }
            FirebaseCrashlytics.getInstance().log(message);
        }
    }
}
