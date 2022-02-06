package com.mobwal.home.utilits;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mobwal.home.DataManager;
import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.models.db.complex.ResultExportItem;
import com.mobwal.home.models.db.Attachment;
import com.mobwal.home.models.db.Point;
import com.mobwal.home.models.db.Route;
import com.mobwal.home.models.db.Template;
import pw.appcode.mimic.*;

/**
 * Генерация архива с результатом работ по маршрут
 */
public class ExportToShared {
    /**
     * идентификатор маршрута по которому создается архив
     */
    private final String mRouteID;
    /**
     * результат генерации
     */
    public File OutputZip;

    public ExportToShared(@NotNull String f_route, @NotNull File outputZip) {
        this.mRouteID = f_route;
    }

    /**
     * Генерация маршрута
     * @param context контекст
     * @return Если строка пустая - архив сформирован без ошибок
     */
    @Nullable
    public String generate(@NotNull Context context, @Nullable ZipManager.ZipListeners listeners) {
        DataManager dataManager = new DataManager(context);
        Route route = dataManager.getRoute(mRouteID);
        FileManager fileManager = new FileManager(context.getCacheDir());

        if (route != null) {
            String routeName = StringUtil.getNameWithOutExtension(route.c_name);
            // оборачиваем еще в одну папку

            String exportName = route.c_catalog == null ? routeName : route.c_catalog;

            File exportFolder = new File(fileManager.getRootCatalog("export"), exportName);
            File rootDir = new File(exportFolder, exportName);
            OutputZip = new File(fileManager.getRootCatalog("export"), exportName + ".zip");

            if(rootDir.exists()) {
                FileManager.deleteRecursive(rootDir);
            }

            if (rootDir.mkdirs()) {
                // формируем каталог для маршрута, который был передан пользователю
                File dataFile = new File(rootDir, "data");

                if (dataFile.mkdir()) {
                    if (route.c_readme != null) {
                        try {
                            fileManager.writeBytes(dataFile, "readme.txt", route.c_readme.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            WalkerApplication.Log("Экспорт. Ошибка создания readme.txt", e);
                            return context.getString(R.string.export_route_error_readme);
                        }
                    }

                    try {
                        fileManager.writeBytes(dataFile, "name.txt", route.c_name.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        WalkerApplication.Log("Экспорт. Ошибка создания name.txt", e);
                        return context.getString(R.string.export_route_error_name);
                    }

                    try {
                        fileManager.writeBytes(dataFile, "id.txt", route.id.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        WalkerApplication.Log("Экспорт. Ошибка создания id.txt", e);
                        return context.getString(R.string.export_route_error_id);
                    }

                    // формирование settings.csv
                    Hashtable<String, String> settings = dataManager.getRouteSettings(mRouteID);
                    if (!settings.isEmpty()) {
                        StringBuilder settingsCSV = new StringBuilder();
                        for (Map.Entry<String, String> item: settings.entrySet()) {
                            settingsCSV.append(MessageFormat.format("{0};{1}", item.getKey(), item.getValue() == null ? "" : item.getValue()));
                            settingsCSV.append("\n");
                        }
                        settingsCSV = settingsCSV.deleteCharAt(settingsCSV.length() - 1);

                        try {
                            fileManager.writeBytes(dataFile, "settings.csv", settingsCSV.toString().getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            WalkerApplication.Log("Экспорт. Ошибка создания settings.csv", e);
                            return context.getString(R.string.export_route_error_settings);
                        }
                    }

                    // формирование tags.csv
                    Template[] templates = dataManager.getTemplates(mRouteID);
                    if (templates != null && templates.length > 0) {
                        StringBuilder tagsCSV = new StringBuilder();
                        for (int i = 0; i < templates.length; i++) {
                            Template template = templates[i];
                            tagsCSV.append(MessageFormat.format("{0};{1};template", template.c_name, template.c_template));
                            if (i != templates.length - 1) {
                                tagsCSV.append("\n");
                            }

                            // формирование шаблонов
                            if (templates[i].c_template != null && templates[i].c_layout != null) {
                                try {
                                    fileManager.writeBytes(dataFile, templates[i].c_template + ".txt", templates[i].c_layout.getBytes(StandardCharsets.UTF_8));
                                } catch (IOException e) {
                                    WalkerApplication.Log("Экспорт. Ошибка создания " + templates[i].c_template + ".txt", e);
                                }
                            }
                        }

                        try {
                            fileManager.writeBytes(dataFile, "tags.csv", tagsCSV.toString().getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            WalkerApplication.Log("Экспорт. Ошибка создания tags.csv", e);
                            return context.getString(R.string.export_route_error_tags);
                        }
                    }

                    // формирование points.csv
                    Point[] points = dataManager.getPoints(mRouteID);
                    if (points != null && points.length > 0) {
                        List<String[]> pointItems = new ArrayList<>();
                        List<String> pointHeaders = new ArrayList<>();

                        pointHeaders.add("c_address");
                        pointHeaders.add("n_latitude");
                        pointHeaders.add("n_longitude");
                        pointHeaders.add("c_description");
                        pointHeaders.add("c_imp_id");
                        pointHeaders.add("b_anomaly");
                        pointHeaders.add("b_check");
                        pointHeaders.add("c_comment");

                        for (Point item : points) {
                            List<String> pointItem = new ArrayList<>();

                            pointItem.add(item.c_address);
                            pointItem.add(normalCsvString(item.n_latitude));
                            pointItem.add(normalCsvString(item.n_longitude));
                            pointItem.add(normalCsvString(item.c_description));
                            pointItem.add(normalCsvString(item.c_imp_id));

                            pointItem.add(normalCsvString(item.b_anomaly));
                            pointItem.add(normalCsvString(!route.b_check || item.b_check));
                            pointItem.add(normalCsvString(item.c_comment));

                            if (!TextUtils.isEmpty(item.jb_data)) {
                                JsonObject jsonObject = JsonParser.parseString(Objects.requireNonNull(item.jb_data)).getAsJsonObject();
                                for (Map.Entry<String, JsonElement> el : jsonObject.entrySet()) {
                                    pointItem.add(el.getValue().getAsString());
                                }
                            }

                            pointItems.add(pointItem.toArray(new String[0]));
                        }

                        StringBuilder pointStr = new StringBuilder(TextUtils.join(";", pointHeaders) + "\n");
                        for (int i = 0; i < pointItems.size(); i++) {
                            pointStr.append(TextUtils.join(";", pointItems.get(i))).append("\n");
                        }

                        try {
                            fileManager.writeBytes(dataFile, "points.csv", StringUtil.trimSymbol(pointStr.toString(), '\n').getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            WalkerApplication.Log("Экспорт. Ошибка создания points.csv", e);
                            return context.getString(R.string.export_route_error_points);
                        }
                    }
                }

                // Результаты
                ResultExportItem[] resultItems = dataManager.getResultExport(mRouteID);
                if (resultItems != null) {
                    Template[] templates = dataManager.getTemplates(mRouteID);

                    if (templates != null) {
                        for (Template template : templates) {
                            List<String> fields = new ArrayList<>();
                            // нужно достать список полей в карточке
                            String layout = SimpleFormLayout.isSimpleLayout(template.c_layout)
                                    ? SimpleFormLayout.convertToMimicUIParser(template.c_layout)
                                    : template.c_layout;

                            if (TextUtils.isEmpty(layout)) {
                                continue;
                            }

                            List<String> pointHeaders = new ArrayList<>();
                            List<String[]> points = new ArrayList<>();

                            pointHeaders.add("f_result_id");
                            pointHeaders.add("c_imp_id");
                            pointHeaders.add("c_address");
                            pointHeaders.add("n_latitude");
                            pointHeaders.add("n_longitude");
                            pointHeaders.add("c_description");
                            pointHeaders.add("b_anomaly");
                            pointHeaders.add("d_result_date");
                            pointHeaders.add("n_result_latitude");
                            pointHeaders.add("n_result_longitude");
                            pointHeaders.add("n_result_distance");
                            pointHeaders.add("c_result_template");
                            pointHeaders.add("c_result_template_name");
                            pointHeaders.add("n_result_image_count");

                            String[] appendFields = SimpleFormLayout.getItems(layout);
                            for (String item : appendFields) {
                                fields.add(item);
                                pointHeaders.add(item);
                            }

                            List<ResultExportItem> filterResults = new ArrayList<>();
                            for (ResultExportItem item : resultItems) {
                                if (item.c_template != null && item.c_template.equals(template.c_template)) {
                                    filterResults.add(item);
                                }
                            }

                            if (filterResults.size() == 0) {
                                continue;
                            }

                            for (int i = 0; i < filterResults.size(); i++) {
                                ResultExportItem item = filterResults.get(i);
                                List<String> pointItem = new ArrayList<>();

                                pointItem.add(item.getId());
                                pointItem.add(normalCsvString(item.c_imp_id));
                                pointItem.add(normalCsvString(item.c_address));
                                pointItem.add(normalCsvString(item.n_latitude));
                                pointItem.add(normalCsvString(item.n_longitude));
                                pointItem.add(normalCsvString(item.c_description));
                                pointItem.add(normalCsvString(item.b_anomaly));

                                pointItem.add(item.d_date == null ? "" : DateUtil.convertDateToSystemString(item.d_date));
                                pointItem.add(normalCsvString(item.n_result_latitude));
                                pointItem.add(normalCsvString(item.n_result_longitude));
                                pointItem.add(item.n_result_distance >= 0 ? normalCsvString(item.n_result_distance) : "");
                                pointItem.add(normalCsvString(item.c_template));
                                pointItem.add(normalCsvString(item.c_template_name));
                                pointItem.add(normalCsvString(item.n_image_count));

                                if (!TextUtils.isEmpty(item.jb_result_data)) {
                                    JsonObject jsonObject = JsonParser.parseString(Objects.requireNonNull(item.jb_result_data)).getAsJsonObject();

                                    for (String field : fields) {
                                        for (Map.Entry<String, JsonElement> el : jsonObject.entrySet()) {
                                            if (field.equals(el.getKey())) {
                                                if (el.getValue().isJsonPrimitive() && el.getValue() == null) {
                                                    pointItem.add("");
                                                } else {
                                                    if (el.getValue().isJsonPrimitive() && el.getValue().getAsJsonPrimitive().isBoolean()) {
                                                        boolean b = el.getValue().getAsBoolean();
                                                        pointItem.add(b ? "true" : "false");
                                                    } else {
                                                        pointItem.add(el.getValue().getAsString());
                                                    }
                                                }
                                            }
                                        }

                                        //pointItem.add(el.getValue().getAsString());
                                    }
                                }

                                points.add(pointItem.toArray(new String[0]));
                            }

                            StringBuilder pointStr = new StringBuilder(TextUtils.join(";", pointHeaders) + "\n");
                            for (int i = 0; i < points.size(); i++) {
                                pointStr.append(TextUtils.join(";", points.get(i))).append("\n");
                            }

                            try {
                                fileManager.writeBytes(rootDir, template.c_template + ".csv", StringUtil.trimSymbol(pointStr.toString(), '\n').getBytes(StandardCharsets.UTF_8));
                            } catch (IOException e) {
                                WalkerApplication.Log("Экспорт. Ошибка создания " + template.c_template + ".csv", e);
                                return context.getString(R.string.export_route_error_results);
                            }
                        }
                    }

                    // Обработка вложений
                    Collection<Attachment> attachmentCollections = dataManager.getRouteAttachments(mRouteID);

                    if (attachmentCollections != null) {
                        Attachment[] attachments = attachmentCollections.toArray(new Attachment[0]);
                        if (attachments.length > 0) {
                            // создать csv
                            List<String[]> files = new ArrayList<>();
                            List<String> fileHeaders = new ArrayList<>();

                            fileHeaders.add("f_result_id");
                            fileHeaders.add("c_name");
                            fileHeaders.add("n_latitude");
                            fileHeaders.add("n_longitude");
                            fileHeaders.add("d_date");
                            fileHeaders.add("n_distance");
                            fileHeaders.add("c_mime");
                            fileHeaders.add("n_size");

                            File photoFile = new File(rootDir, "attachments");
                            if(!photoFile.mkdir()) {
                                WalkerApplication.Debug("Экспорт. Ошибка создания каталога attachments");
                            }

                            for (ResultExportItem resultItem : resultItems) {
                                List<Attachment> filterAttachments = new ArrayList<>();
                                for (Attachment attachment :
                                        attachments) {
                                    if (attachment.f_result.equals(resultItem.f_result)) {
                                        filterAttachments.add(attachment);
                                    }
                                }

                                for (int j = 0; j < filterAttachments.size(); j++) {
                                    List<String> fileItem = new ArrayList<>();

                                    Attachment attachment = filterAttachments.get(j);

                                    fileItem.add(resultItem.getId());
                                    String picName = resultItem.getId() + "-" + (j + 1) + StringUtil.getFileExtension(attachment.c_name);
                                    fileItem.add(picName);
                                    fileItem.add(normalCsvString(attachment.n_latitude));
                                    fileItem.add(normalCsvString(attachment.n_longitude));
                                    fileItem.add(DateUtil.convertDateToSystemString(attachment.d_date));
                                    fileItem.add(attachment.n_distance != null ? normalCsvString(attachment.n_distance) : "");

                                    FileManager byFile = new FileManager(context.getFilesDir());
                                    File url = new File(byFile.getRootCatalog(mRouteID), attachment.c_name);

                                    if (url.exists()) {
                                        fileManager.copy(url, new File(photoFile, picName));

                                        fileItem.add(ActivityUtil.getMimeType(context, Uri.fromFile(url)));
                                        fileItem.add(String.valueOf(url.length()));
                                    }

                                    files.add(fileItem.toArray(new String[0]));
                                }
                            }

                            if (files.size() > 0) {
                                StringBuilder pointStr = new StringBuilder(TextUtils.join(";", fileHeaders) + "\n");
                                for (int i = 0; i < files.size(); i++) {
                                    pointStr.append(TextUtils.join(";", files.get(i))).append("\n");
                                }

                                try {
                                    fileManager.writeBytes(rootDir, "attachments.csv", StringUtil.trimSymbol(pointStr.toString(), '\n').getBytes(StandardCharsets.UTF_8));
                                } catch (IOException e) {
                                    WalkerApplication.Log("Экспорт. Ошибка создания attachments.csv", e);
                                    return context.getString(R.string.export_route_error_results);
                                }
                            }
                        }
                    }
                }

                // Архивирование результат
                if(OutputZip.exists()) {
                    if(!OutputZip.delete()) {
                        WalkerApplication.Debug("Экспорт. Ошибка удаления архива.");
                    }
                }

                ZipManager.zip(context, exportFolder, OutputZip.getPath(), listeners);

                FileManager.deleteRecursive(exportFolder);

                if(!OutputZip.exists()) {
                    return context.getString(R.string.export_route_error_not_found_zip);
                }

                if (!dataManager.exportRoute(mRouteID)) {
                    return context.getString(R.string.route_update_error);
                }
            }
        }

        return null;
    }

    private <T> String normalCsvString(T object) {
        if(object == null) {
            return "";
        }
        return String.valueOf(object);
    }
}
