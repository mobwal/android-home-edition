package com.mobwal.home.utilits;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.mobwal.home.Names;
import com.mobwal.home.WalkerApplication;

public class ImageUtil {

    public static Bitmap rotateImage(String photoPath, Bitmap bitmap) {
        ExifInterface ei;
        try {
            ei = new ExifInterface(photoPath);
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = getImageRotated(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = getImageRotated(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = getImageRotated(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }

    public static Bitmap getImageRotated(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static byte[] bitmapToBytes(Bitmap source) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        source.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        source.recycle();
        stream.close();
        return  byteArray;
    }

    /**
     * сжатие изображения
     *
     * @param inputStream поток
     * @param quality     качество сжатия от о до 100
     * @return сжатие данные
     */
    public static byte[] compress(InputStream inputStream, int quality, int MAX_IMAGE_HEIGHT) {
        try {
            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
            if (bmp == null) {
                return null;
            }

            Bitmap resizeBmp = scaleToFitHeight(bmp, MAX_IMAGE_HEIGHT);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            resizeBmp.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            return bos.toByteArray();
        } catch (Exception e) {
            WalkerApplication.Log("Ошибка в сжатии изображения", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    WalkerApplication.Log("Ошибка при сжатии изображения. Ошибка закрытия потока.", e);
                }
            }
        }

        return null;
    }

    // Scale and maintain aspect ratio given a desired height
    // BitmapScale.scaleToFitHeight(bitmap, 100);
    public static Bitmap scaleToFitHeight(Bitmap b, int height) {
        float factor = height / (float) b.getHeight();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
    }

    /**
     * Размытть изображение
     * @param image изображение
     * @return размытое изображение
     */
    public static Bitmap blur(Context context, Bitmap image) {
        float BITMAP_SCALE = 0.4f;
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        float BLUR_RADIUS = 7.5f;
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    /**
     * механизм оптимального сжатия изображения для вывода пользователю
     * @param data массив байтов
     * @param offset
     * @param length
     * @param desiredWidth размер изображения
     * @return
     */
    public static Bitmap getSizedBitmap(byte[] data, int offset, int length, int desiredWidth) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        options.inSampleSize = 1;
        options.inJustDecodeBounds = false;

        if(options.outWidth > desiredWidth) {
            final int halfWidth = options.outWidth / 2;
            while (halfWidth / options.inSampleSize > desiredWidth) {
                options.inSampleSize *= 2;
            }
        }

        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }
}
