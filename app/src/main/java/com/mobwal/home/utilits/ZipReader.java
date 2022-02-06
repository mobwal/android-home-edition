package com.mobwal.home.utilits;

import android.content.Context;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.mobwal.home.WalkerApplication;
import com.mobwal.home.models.db.Point;

/**
 * Чтение zip файла.
 */
public class ZipReader {
    private static final String POINTS = "points.csv";
    private static final String SETTINGS = "settings.csv";
    private static final String TAGS = "tags.csv";
    private static final String README = "readme.txt";
    private static final String ID = "id.txt";
    private static final String NAME = "name.txt";
    private static final String DATA = "data";

    private File mOutputDir;
    private final File mZipFile;

    public File getOutputDir() {
        return mOutputDir;
    }

    /**
     * Конструктор
     * @param context контекст
     * @param zipPath путь к архиву ZIP
     * @param output куда распаковать
     * @param listeners обработчик событий
     */
    public ZipReader(@NotNull Context context, @NotNull String zipPath, @Nullable String output, @Nullable ZipManager.ZipListeners listeners) {
        mZipFile = new File(zipPath);

        if(mZipFile.exists()) {
            String result = ZipManager.unzip(context, zipPath, output, listeners);
            if(output == null) {
                mOutputDir = getCatalog(mZipFile);
            } else {
                mOutputDir = new File(output);
            }

            if(!TextUtils.isEmpty(result)) {
                // если была ошибка, то все откатываем
                if(mOutputDir != null && mOutputDir.exists()) {
                    FileManager.deleteRecursive(mOutputDir);
                }
            }
        }
    }

    @Nullable
    private File getCatalog(File zipFile) {
        File parent = zipFile.getParentFile();
        if(parent != null) {
            File[] files = parent.listFiles();
            if(files != null) {
                for (File file :
                        files) {
                    if (file.isDirectory() &&
                        !file.getName().equals("com.google.android.gms.maps.volley")) {
                        return file;
                    }
                }
            }
        }

        return null;
    }

    /**
     * распаковка прошла успешно
     * @return true - данные распакованны
     */
    public boolean isExtracted() {
        return mOutputDir.exists();
    }

    /**
     * Режим для проверки работы
     * @return true - режим проверки
     */
    public boolean isCheckMode() {
        File dataDir = new File(mOutputDir, DATA);
        if(dataDir.exists() && dataDir.isDirectory()) {
            String[][] rows = getPoints(true);
            if(rows != null) {
                Point[] points = ImportUtil.convertRowsToPointsFromResults(rows, "");
                if(points != null) {
                    int count = 0;
                    for (Point point : points) {
                        if (!point.b_check) {
                            count++;
                        }
                    }
                    return count > 0;
                }
            }
        }
        return false;
    }

    /**
     * получение настроек
     * @param data просмотр в каталоге data. По умолчанию false
     * @return описание
     */
    @Nullable
    public String[][] getPoints(boolean data) {
        File file = new File (data ? new File(mOutputDir, DATA) : mOutputDir, POINTS);
        String content = getContent(file);

        if(TextUtils.isEmpty(content)) {
            return null;
        } else {
            assert content != null;
            return new CsvReader(content).getRows(data);
        }
    }

    /**
     * получение настроек
     * @param data просмотр в каталоге data. По умолчанию false
     * @return описание
     */
    @Nullable
    public String[][] getSettings(boolean data) {
        File file = new File (data ? new File(mOutputDir, DATA) : mOutputDir, SETTINGS);
        String content = getContent(file);

        if(TextUtils.isEmpty(content)) {
            return null;
        } else {
            assert content != null;
            return new CsvReader(content).getRows();
        }
    }

    /**
     * чтение описания маршрута
     * @param data просмотр в каталоге data. По умолчанию false
     * @return описание
     */
    @Nullable
    public String[][] getTags(boolean data) {
        File file = new File (data ? new File(mOutputDir, DATA) : mOutputDir, TAGS);
        String content = getContent(file);

        if(TextUtils.isEmpty(content)) {
            return null;
        } else {
            assert content != null;
            return new CsvReader(content).getRows();
        }
    }

    /**
     * чтение формы
     * @param name имя формы
     * @param data просмотр в каталоге data. По умолчанию false
     * @return форма
     */
    @Nullable
    public String getForm(String name, boolean data) {
        File file = new File (data ? new File(mOutputDir, DATA) : mOutputDir, name + ".txt");
        return getContent(file);
    }

    /**
     * чтение описания маршрута
     * @param data просмотр в каталоге data. По умолчанию false
     * @return описание
     */
    @Nullable
    public String getReadme(boolean data) {
        File file = new File (data ? new File(mOutputDir, DATA) : mOutputDir, README);
        return getContent(file);
    }

    /**
     * чтение наименование маршрута
     * @param data просмотр в каталоге data. По умолчанию false
     * @return наименование
     */
    public String getName(boolean data) {
        File file = new File (data ? new File(mOutputDir, DATA) : mOutputDir, NAME);
        String content = getContent(file);
        return TextUtils.isEmpty(content) ? getOutputDir().getName() : content;
    }

    /**
     * чтение id маршрута
     * @param data просмотр в каталоге data. По умолчанию false
     * @return описание
     */
    @Nullable
    public String getId(boolean data) {
        File file = new File (data ? new File(mOutputDir, DATA) : mOutputDir, ID);
        return getContent(file);
    }

    /**
     * Получение данных из файла в текстовом файле
     * @param file файл для обработки
     * @return строка с данными
     */
    @Nullable
    private String getContent(@NotNull File file) {
        if(file.exists()) {
            try (FileInputStream inputStream = new FileInputStream(file)) {
                return new String(StreamUtil.readBytes(inputStream), StandardCharsets.UTF_8);
            } catch (IOException ignored) {

            }
        }

        return null;
    }

    /**
     * получение списка строк
     * @param name имя файла для чтения результатов
     * @return словарь
     */
    @Nullable
    public String[][] getArrayFromCSV(@NotNull String name) {
        File file = new File(mOutputDir, name);
        String content = getContent(file);
        if(TextUtils.isEmpty(content)) {
            return null;
        } else {
            assert content != null;
            return new CsvReader(content).getRows();
        }
    }

    /**
     * Путь к вложение
     * @param fileName имя файла
     * @return путь к вложению
     */
    @Nullable
    public File getAttachmentUrl(@NotNull String fileName) {
        File attach = new File(mOutputDir, "attachments");
        File file = new File(attach, fileName);
        if(file.exists()) {
            return file;
        }

        return null;
    }

    /**
     * удаление распакованного каталога
     */
    public void close() {
        if(mZipFile != null && mZipFile.exists()) {
            if(mZipFile.delete()) {
                WalkerApplication.Debug("Ошибка удаления архива.");
            }
        }

        if(mOutputDir != null && mOutputDir.exists()) {
            FileManager.deleteRecursive(mOutputDir);
        }
    }
}
