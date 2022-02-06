package com.mobwal.home.utilits;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.webkit.MimeTypeMap;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.MessageFormat;
import java.util.Locale;

import com.mobwal.home.R;

public class ActivityUtil {
    /**
     * Сообщение нет доступа к функционалу
     * @param context контекст
     * @param features наименование функций
     * @return строка
     */
    public static String getMessageNotGranted(Context context, String[] features) {

        return context.getString(R.string.permissions_not_granted) +
                ": " +
                TextUtils.join(", ", features) + ".";
    }

    /**
     * Получение intent для переходан на настройки приложения
     * @param context контекст
     * @return intent
     */
    public static Intent getIntentApplicationSetting(Context context) {
        // https://ourcodeworld.com/articles/read/318/how-to-open-android-settings-programmatically-with-java
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);

        return intent;
    }

    /**
     * Преобразование координат в пользовательскую строку
     * @param location геолокация
     * @return строка
     */
    public static String toUserString(@Nullable  Location location) {
        if(location != null) {
            return String.format(Locale.getDefault(), "%.6f : %.6f", location.getLongitude(), location.getLatitude());
        }

        return "";
    }

    public static int getColor(
            Context context,
            int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = context.getResources().getColor(colorRes);
        } catch (Resources.NotFoundException e) {
            Log.w("COLOR", "Not found color resource by id: " + colorRes);
        }
        return color;
    }

    /**
     * Тип файла Mime
     * @param context контекст
     * @param uri путь
     * @return MIME
     */
    @Nullable
    public static String getMimeType(Context context, Uri uri) {
        String mimeType;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    /**
     * Открыть изображение в галереи
     * @param context контекст
     * @param fileImage файл для просмотра
     */
    public static void openGallery(Context context, File fileImage) {
        if(fileImage.exists()) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri photoURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", fileImage);
            intent.setDataAndType(photoURI, "image/*");
            context.startActivity(intent);
        }
    }

    @NotNull
    public static String getFileNameFromUri(@NotNull Uri uri) {
        String path = uri.getPath();
        int i = path.lastIndexOf("/");
        return path.substring(i + 1);
    }

    /**
     * Открытие веб страницы
     * @param context контекст
     * @param url страница сайта
     */
    public static void openWebPage(@NotNull Context context, @NotNull String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    /**
     * Открыть экран шаблона формы
     * @param activity активность
     */
    public static void openLayout(@NotNull Activity activity) {
        NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_layout);
    }

    /**
     * Открыть экран маршрутов
     * @param activity активность
     */
    public static void openRoutes(@NotNull Activity activity) {
        NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_route);
    }

    public static void openFileChooser(@NotNull Activity activity, int code) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimetypes = {"text/plain", "application/zip", "text/comma-separated-values", "text/csv"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

        Intent chosenIntent = Intent.createChooser(intent, activity.getString(R.string.import_name));
        activity.startActivityForResult(chosenIntent, code);
    }
}
