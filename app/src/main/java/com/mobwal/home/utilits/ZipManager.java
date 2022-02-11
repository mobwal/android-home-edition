package com.mobwal.home.utilits;

import android.content.Context;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;

/**
 * Вспомагательный класс для работы с архивами ZIP
 */
public class ZipManager {
    private static final int BUFFER_SIZE = 4 * 1024;

    public static String zip(@NotNull Context context, File dir, String outputFile) {
        return zip(context, dir, outputFile, null);
    }

    @Nullable
    private static File[] walk(File root) {
        List<File> files = new ArrayList<>();

        File[] list = root.listFiles();

        if(list != null) {
            for (File f : list) {
                if (f.isDirectory()) {
                    File[] items = walk(f);
                    if(items != null) {
                        files.addAll(Arrays.asList(items));
                    }
                } else {
                    files.add(f);
                }
            }

            return files.toArray(new File[0]);
        }

        return null;
    }

    /**
     * Сжатие каталога
     * @param dir директория для сжатия
     * @param outputFile выходной результат
     * @return Если пустая строка, то результат обработки прошел без ошибок
     */
    @Nullable
    public static String zip(@NotNull Context context, File dir, String outputFile, @Nullable ZipListeners listeners) {
        if(!dir.exists()) {
            return context.getString(R.string.zip_error);
        }

        File[] files = walk(dir);

        if(files != null) {

            int total = files.length;
            int current = 0;

            BufferedInputStream origin;

            try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {

                for (File f : files) {
                    FileInputStream fi = new FileInputStream(f);
                    origin = new BufferedInputStream(fi, BUFFER_SIZE);
                    try {
                        String path = f.getAbsolutePath().replace(dir.getAbsolutePath(), "");
                        if(path.startsWith(File.separator)) {
                            path = path.substring(1);
                        }
                        ZipEntry entry = new ZipEntry(path);
                        out.putNextEntry(entry);
                        int count;
                        byte[] data = new byte[BUFFER_SIZE];
                        while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                            out.write(data, 0, count);
                        }
                    } finally {
                        origin.close();
                    }

                    current++;

                    if(listeners != null) {
                        listeners.onZipPack(total, current);
                    }
                }
            } catch (IOException e) {
                WalkerApplication.Log("Ошибка упаковки архива", e);
                return context.getString(R.string.unknown_error) + " ZIP0";
            }
        }

        return null;
    }

    public static String unzip(@NotNull Context context, @NotNull String zipFile, @Nullable String output) {
        return unzip(context, zipFile, output, null);
    }

    /**
     * Распаковка файла
     * @param zipFile путь к архиву
     * @param output выходной результат. Если передать null, то будет распаковка в том же каталоге, что и файл
     * @param listeners обработчик событий
     * @return Если пустая строка, то результат обработки прошел без ошибок
     */
    @Nullable
    public static String unzip(@NotNull Context context, @NotNull String zipFile, @Nullable String output, @Nullable ZipListeners listeners) {
        try {
            File file = new File(zipFile);
            if(TextUtils.isEmpty(output)) {
                output = file.getParent();
            }
            int total = 0;

            try (ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry ze;
                while ((ze = zin.getNextEntry()) != null) {
                    if (!ze.isDirectory()) {
                        total++;
                    }
                }
            }

            int current = 0;

            try (ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry ze;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = output + (ze.getName().startsWith(File.separator) ? ze.getName() : File.separator + ze.getName());
                    File unZipFile = new File(path);
                    assert output != null;
                    if (!path.startsWith(output)) {
                        throw new SecurityException();
                    }

                    if (ze.isDirectory()) {
                        if (!unZipFile.isDirectory()) {
                            if(!unZipFile.mkdirs()) {
                                return context.getString(R.string.unzip_error) + " UNZIP3";
                            }
                        }
                    } else {
                        if(unZipFile.getParentFile() != null
                                && !unZipFile.getParentFile().exists()) {
                            if(!unZipFile.getParentFile().mkdirs()) {
                                return context.getString(R.string.unzip_error) + " UNZIP4";
                            }
                        }

                        try (FileOutputStream fout = new FileOutputStream(path, false)) {
                            byte[] data = new byte[BUFFER_SIZE];
                            int count;
                            while ((count = zin.read(data)) != -1) {
                                fout.write(data, 0, count);
                            }
                            zin.closeEntry();
                        }

                        current++;
                    }

                    if(listeners != null) {
                        listeners.onZipUnPack(total, current);
                    }
                }
            }
        } catch (Exception e) {
            WalkerApplication.Log("Ошибка распаковки архива", e);
            return context.getString(R.string.unknown_error) + " UNZIP6";
        }

        return null;
    }

    public interface ZipListeners {
        default void onZipUnPack(int total, int current) {

        }

        default void onZipPack(int total, int current) {

        }
    }
}
