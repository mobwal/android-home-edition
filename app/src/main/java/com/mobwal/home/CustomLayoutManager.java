package com.mobwal.home;

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.mobwal.home.utilits.StreamUtil;

public class CustomLayoutManager {
    private final String mFileName = "CUSTOM.txt";
    private final File mDir;
    private final Context mContext;

    public CustomLayoutManager(@NotNull Context context) {
        mDir = context.getCacheDir();
        mContext = context;
    }

    /**
     * получение имени шаблона
     * @return имя шаблона для отображения
     */
    @NotNull
    public String getDefaultLayoutName() {
        File file = new File(mDir, mFileName);
        if(file.exists()) {
            return "CUSTOM";
        } else {
            return "DEFAULT";
        }
    }

    /**
     * получение разметки шаблона
     * @return разметка шаблона для отображения
     */
    @Nullable
    public String getDefaultLayout() {
        File file = new File(mDir, mFileName);
        if (file.exists()) {
            try (FileInputStream inputStream = new FileInputStream(file)) {
                return new String(StreamUtil.readBytes(inputStream), StandardCharsets.UTF_8);
            } catch (IOException e) {
                WalkerApplication.Log("Ошибка чтения шаблона.", e);
            }
        }

        try (InputStream inStream = mContext.getResources().openRawResource(R.raw.default_layout)) {
            return new String(StreamUtil.readBytes(inStream), StandardCharsets.UTF_8);
        } catch (IOException e) {
            WalkerApplication.Log("Ошибка чтения шаблона по умолчанию.", e);
        }

        return null;
    }

    /**
     * Удаление шаблона
     * @return результат удаления
     */
    public boolean removeLayout() {
        File file = new File(mDir, mFileName);

        if(file.exists()) {
            return file.delete();
        }

        return false;
    }

    /**
     * Обновление локального шаблона
     * @param content шаблон
     * @return true - обновление произведено
     */
    public boolean updateLayout(@NotNull String content) {
        try {
            File file = new File(mDir, mFileName);
            FileOutputStream outputStream;
            BufferedOutputStream bos = null;
            try {
                outputStream = new FileOutputStream(file);
                bos = new BufferedOutputStream(outputStream);
                byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                bos.write(bytes, 0, bytes.length);
                return true;
            } catch (IOException e) {
                WalkerApplication.Log("Ошибка обновления шаблона форм.", e);
            } finally {
                if(bos != null) {
                    bos.flush();
                    bos.close();
                }
            }
        } catch (IOException e) {
            WalkerApplication.Log("Общая ошибка обновления шаблона форм.", e);
        }

        return false;
    }
}
