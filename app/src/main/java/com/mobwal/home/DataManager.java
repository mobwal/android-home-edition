package com.mobwal.home;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import com.mobwal.home.models.PointInfo;
import com.mobwal.home.models.db.complex.PointItem;
import com.mobwal.home.models.db.complex.ResultExportItem;
import com.mobwal.home.models.db.complex.ResultTemplate;
import com.mobwal.home.models.RouteInfo;
import com.mobwal.home.models.db.complex.RouteItem;
import com.mobwal.home.models.db.Attachment;
import com.mobwal.home.models.db.Point;
import com.mobwal.home.models.db.Result;
import com.mobwal.home.models.db.Route;
import com.mobwal.home.models.db.Setting;
import com.mobwal.home.models.db.Template;
import com.mobwal.home.utilits.DateUtil;
import com.mobwal.home.utilits.FileManager;

public class DataManager {
    private final Context mContext;

    public DataManager(Context context) {
        mContext = context;
    }

    /**
     * Получение списка маршрутов
     * @param search посковое слово
     * @return результат выборки
     */
    @Nullable
    public RouteItem[] getRoutes(@Nullable String search) {

        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<RouteItem> collection;

        String query = "SELECT r.id as ID, r.c_name as C_NUMBER, (select count(*) from POINT as p where p.f_route = r.id) as N_TASK, (select count(*) from POINT as p where p.f_route = r.id and p.b_anomaly = 1) as N_ANOMALY, (select count (*) from (select p.id from POINT as p inner join RESULT as rr on rr.f_point = p.id where p.f_route = r.id and p.b_check = 1 group by p.id) as t) as N_DONE, (select count (*) from (select p.id from POINT as p where p.f_route = r.id and p.b_check = 0) as t) as N_FAIL, r.d_date as D_DATE, r.B_EXPORT, r.c_readme as C_README, r.b_check as B_CHECK from Route as r" + (TextUtils.isEmpty(search) ? "" : " where r.c_name like '%' || ? || '%'") + " order by r.n_date desc";

        if(TextUtils.isEmpty(search)) {
            collection = sqlContext.select(query, null, RouteItem.class);
        } else {
            collection = sqlContext.select(query, new String[] { search }, RouteItem.class);
        }
        if(collection != null) {
            return collection.toArray(new RouteItem[0]);
        } else {
            return null;
        }
    }

    /**
     * Получение настроек маршрута
     * @param f_route идентификатор маршрута
     * @return список настроек
     */
    @NotNull
    public Hashtable<String, String> getRouteSettings(String f_route) {
        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<Setting> settings = sqlContext.select("select * from setting where f_route = ?", new String[] { f_route }, Setting.class);

        Hashtable<String, String> hashtable = new Hashtable<>();
        if (settings != null) {
            for (Setting setting : settings) {
                hashtable.put(setting.c_key, setting.c_value);
            }
        } else {
            hashtable.put("geo", "false");
            hashtable.put("geo_quality", "LOW");
            hashtable.put("image", "true");
            hashtable.put("image_quality", "0.6");
            hashtable.put("image_height", "720");
        }
        return hashtable;
    }

    /**
     * Получение информации по маршруту
     * @param f_route идентификатор маршрута
     * @return данные по маршруту
     */
    public RouteInfo[][] getRouteInfo(@Nullable String f_route) {
        RouteInfo[][] results = new RouteInfo[2][];

        if(f_route == null) {
            return results;
        }

        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<Route> routeCollection = sqlContext.select("select * from Route as r where r.id = ?", new String[] { f_route }, Route.class);

        List<RouteInfo> items = new ArrayList<>();

        if(routeCollection != null && routeCollection.size() > 0) {
            Route[] routes = routeCollection.toArray(new Route[0]);

            if(routes[0].d_date != null) {
                items.add(new RouteInfo(mContext, mContext.getString(R.string.in_work), DateUtil.toDateTimeString(routes[0].d_date)));
            }

            if(routes[0].d_export != null) {
                items.add(new RouteInfo(mContext, mContext.getString(R.string.exported), DateUtil.toDateTimeString(routes[0].d_export)));
            }

            results[0] = items.toArray(new RouteInfo[0]);

            items.clear();

            if(routes[0].c_readme != null) {
                items.add(new RouteInfo(mContext, mContext.getString(R.string.description), routes[0].c_readme));
            }
        }

        Collection<Setting> settingCollection = sqlContext.select("select * from Setting as s where s.f_route = ? order by s.c_key", new String[] { f_route }, Setting.class);
        
        if(settingCollection != null && settingCollection.size() > 0) {
            for (Setting setting:
                 settingCollection) {
                items.add(new RouteInfo(mContext, setting.toKeyName(mContext), setting.c_value));
            }

            results[1] = items.toArray(new RouteInfo[0]);
        } else {
            if(items.size() > 0) {
                results[1] = items.toArray(new RouteInfo[0]);
            } else {
                results[1] = null;
            }
        }

        return results;
    }

    /**
     * Получение маршрута
     * @param f_route идентификатор маршрута
     * @return маршрут
     */
    @Nullable
    public Route getRoute(@Nullable String f_route) {
        if(TextUtils.isEmpty(f_route)) {
            return null;
        }

        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<Route> routeCollection = sqlContext.select("SELECT * from Route as r where r.id = ?", new String[] { f_route }, Route.class);

        if(routeCollection != null && routeCollection.size() > 0) {
            return routeCollection.toArray(new Route[0])[0];
        }

        return null;
    }

    /**
     * Создание точки
     * @param f_route идентификатор маршрута
     * @param name наименование точки
     * @param desc описание точки
     * @param location геолокация
     * @return результат создания
     */
    public boolean createPoint(@NotNull String f_route, @NotNull String name, @Nullable String desc, @Nullable Location location) {
        // тут нужно создать точку с самым низким приоритетом
        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);

        Point point = new Point();
        point.f_route = f_route;
        point.c_address = name;
        point.c_description = desc;
        if(location != null) {
            point.n_longitude = location.getLongitude();
            point.n_latitude = location.getLatitude();
        }

        Point maxPoint = getPointMaxOrder(f_route);
        if(maxPoint != null) {
            point.n_order = maxPoint.n_order + 1;
        } else {
            point.n_order = 1;
        }

        point.b_anomaly = true;

        return sqlContext.insertMany(new Point[] { point });
    }

    /**
     * Получение точки с максимальным order'ом
     * @param f_route иден. маршрута
     * @return точка
     */
    @Nullable
    private Point getPointMaxOrder(@NotNull String f_route) {
        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<Point> pointCollection = sqlContext.select("SELECT * from Point as p where p.f_route = ? order by p.n_order desc limit 1", new String[] { f_route }, Point.class);

        if(pointCollection != null && !pointCollection.isEmpty()) {
            return pointCollection.toArray(new Point[0])[0];
        }

        return null;
    }

    /**
     * Получен точек по маршруту
     * @param f_route идентификатор маршрута
     * @param search поисковый запрос
     * @return результат выборки
     */
    @Nullable
    public PointItem[] getPoints(@NotNull String f_route, @Nullable String search) {

        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<PointItem> collection;

        String query = "SELECT p.ID, p.C_DESCRIPTION, p.JB_DATA, p.C_ADDRESS, (select count(*) from RESULT as rr where rr.f_point = p.id) > 0 as B_DONE, p.B_ANOMALY, p.B_CHECK from Point as p where p.f_route = ?" + (TextUtils.isEmpty(search) ? "" : " and p.c_address like '%' || ? || '%'") + " order by p.n_order";

        if(TextUtils.isEmpty(search)) {
            collection = sqlContext.select(query, new String[] { f_route }, PointItem.class);
        } else {
            collection = sqlContext.select(query, new String[] { f_route, search }, PointItem.class);
        }
        if(collection != null) {
            return collection.toArray(new PointItem[0]);
        } else {
            return null;
        }
    }

    /**
     * Получен точек по маршруту
     * @param f_route идентификатор маршрута
     * @return результат выборки
     */
    public Point[] getPoints(@NotNull String f_route) {
        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<Point> collection = sqlContext.select("SELECT * from Point as p where p.f_route = ? order by p.n_order", new String[] { f_route }, Point.class);

        if(collection != null) {
            return collection.toArray(new Point[0]);
        } else {
            return null;
        }
    }

    /**
     * Получение информации по точке
     * @param f_point идентифкатор точки
     * @return результат
     */
    @Nullable
    public PointInfo[] getPointInfo(@NotNull String f_point) {

        Point point = getPoint(f_point);
        if(point != null) {
            List<PointInfo> items = new ArrayList<>();
            items.add(new PointInfo(mContext, mContext.getString(R.string.address), point.c_address));
            items.add(new PointInfo(mContext, mContext.getString(R.string.description), point.c_description));


            WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
            Collection<ResultTemplate> collection = sqlContext.select("select t.id as F_TEMPLATE, t.C_NAME as C_TEMPLATE, t.C_TEMPLATE as C_CONST, r.id as F_RESULT, r.d_date as D_DATE from RESULT as r left join TEMPLATE as t on r.c_template = t.c_template where t.f_route = ? and r.f_point = ? and r.id is not null", new String[] { point.f_route, f_point }, ResultTemplate.class);

            if(collection != null && !collection.isEmpty()) {
                ResultTemplate[] templates = collection.toArray(new ResultTemplate[0]);
                for (ResultTemplate resultTemplate : templates) {
                    if(resultTemplate.isExistsResult()) {
                        
                        PointInfo pointInfo = new PointInfo(mContext, DateUtil.toDateTimeString(resultTemplate.d_date), MessageFormat.format("{0} - {1}", mContext.getString(R.string.done), resultTemplate.c_template));
                        pointInfo.result = resultTemplate.f_result;
                        items.add(pointInfo);
                    }
                }
            }

            return items.toArray(new PointInfo[0]);
        }

        return null;
    }

    /**
     * Получен результата по точке
     * @param f_point идентификатор точки
     * @return результат по точке
     */
    @Nullable
    public Result[] getResults(@NotNull String f_point) {

        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<Result> collection = sqlContext.select("SELECT * from Result as r where r.f_point = ? order by r.n_date", new String[] { f_point }, Result.class);

        if(collection != null) {
            return collection.toArray(new Result[0]);
        } else {
            return null;
        }
    }

    /**
     * Получен количества результатов по маршруту
     * @param f_route идентификатор маршрута
     * @return количество точек
     */
    @Nullable
    public Long getResultCount(@Nullable String f_route) {
        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        return sqlContext.count("SELECT count(*) from Result as r where r.f_route = ?", new String[] { f_route });
    }

    /**
     * Получение списка шаблонов для точки маршрутов
     * @param f_route иден. маршрута
     * @param f_point идентификатор точки
     * @return списко шаблонов
     */
    @Nullable
    public ResultTemplate[] getResultTemplates(@NotNull String f_route, @NotNull String f_point) {

        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<ResultTemplate> collection = sqlContext.select("select t.id as F_TEMPLATE, t.C_NAME as C_TEMPLATE, t.C_TEMPLATE as C_CONST, r.id as F_RESULT, r.d_date as D_DATE from TEMPLATE as t left join RESULT as r on r.c_template = t.c_template and r.f_point = ? where t.f_route = ?", new String[] { f_point, f_route }, ResultTemplate.class);

        if(collection != null && !collection.isEmpty()) {
            return collection.toArray(new ResultTemplate[0]);
        }

        return null;
    }

    @Nullable
    public Template[] getTemplates(@NotNull String f_route) {
        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<Template> resultCollection = sqlContext.select("SELECT * from Template as t where t.f_route = ?", new String[] { f_route }, Template.class);

        if(resultCollection != null && resultCollection.size() > 0) {
            return resultCollection.toArray(new Template[0]);
        }

        return null;
    }

    /**
     * Результат по точки
     * @param f_result иден. точки
     * @return результат
     */
    @Nullable
    public Result getResult(@Nullable String f_result) {

        if(f_result == null) {
            return null;
        }

        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<Result> resultCollection = sqlContext.select("SELECT * from Result as r where r.id = ?", new String[] { f_result }, Result.class);

        if(resultCollection != null && resultCollection.size() > 0) {
            return resultCollection.toArray(new Result[0])[0];
        }

        return null;
    }

    /**
     * получение точки
     * @param f_point иден. точки
     * @return точка
     */
    @Nullable
    public Point getPoint(@Nullable String f_point) {
        if(f_point == null) {
            return null;
        }

        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<Point> pointCollection = sqlContext.select("SELECT * from Point as p where p.id = ?", new String[] { f_point }, Point.class);

        if(pointCollection!= null && !pointCollection.isEmpty()) {
            return pointCollection.toArray(new Point[0])[0];
        }

        return null;
    }

    public boolean exportRoute(@NotNull String f_route) {
        Route route = getRoute(f_route);
        if(route != null) {
            route.b_export = true;
            route.d_export = new Date();

            WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
            return sqlContext.insertMany(new Route[] { route });
        }
        return false;
    }

    @Nullable
    public Collection<Attachment> getAttachments(@Nullable String f_result) {
        if(f_result == null) {
            return null;
        }

        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);

        return sqlContext.select("SELECT * from ATTACHMENT as a where a.f_result = ? order by a.n_date asc", new String[] { f_result }, Attachment.class);
    }

    @Nullable
    public Collection<Attachment> getRouteAttachments(@NotNull String f_route) {
        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);

        return sqlContext.select("SELECT * from ATTACHMENT as a where a.f_route = ? order by a.n_date asc", new String[] { f_route }, Attachment.class);
    }

    /**
     * Обновление вложений
     * @param f_result идентификатор результата
     * @param attachments вложения
     * @return true - добавление прошло успешно
     */
    public boolean updateAttachments(@NotNull String f_result, @NotNull Attachment[] attachments) {
        // тут вначале удалем все вложения с f_result
        // потом сожаем текущие в БД
        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);

        Collection<Attachment> collection = getAttachments(f_result);
        FileManager mFileManager = new FileManager(mContext.getFilesDir());
        if(collection != null) {
            for (Attachment attachment:
                 collection) {
                mFileManager.deleteFile(attachment.f_route, attachment.c_name + ".jpg");
            }
        }

        for (Attachment attachment : attachments) {
            attachment.f_result = f_result;
        }

        return sqlContext.exec("delete from ATTACHMENT where f_result = ?;", new String[] { f_result }) &&
                sqlContext.insertMany(attachments);
    }

    public boolean delRoute(@NotNull String f_route) {
        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        FileManager fileManager = new FileManager(mContext.getFilesDir());
        fileManager.deleteFolder(f_route);

        return sqlContext.exec("delete from TEMPLATE where f_route = ?", new String[]{f_route})
                && sqlContext.exec("delete from SETTING where f_route = ?", new String[]{f_route})
                && sqlContext.exec("delete from ATTACHMENT where f_route = ?;", new String[]{f_route})
                && sqlContext.exec("delete from RESULT where f_route = ?;", new String[]{f_route})
                && sqlContext.exec("delete from POINT where f_route = ?;", new String[]{f_route})
                && sqlContext.exec("delete from ROUTE where id = ?;", new String[]{f_route});
    }

    public boolean delPoint(@NotNull String f_point) {
        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<Attachment> collection = sqlContext.select("select * from ATTACHMENT where f_point = ?;", new String[] { f_point }, Attachment.class);

        if(collection != null) {
            Attachment[] array = collection.toArray(new Attachment[0]);
            if(array.length > 0) {
                String f_route = array[0].f_route;
                FileManager fileManager = new FileManager(mContext.getFilesDir());
                for (Attachment item: array) {
                    fileManager.deleteFile(f_route, item.c_name);
                }
            }
        }

        if(sqlContext.exec("delete from ATTACHMENT where f_point = ?;", new String[] { f_point })) {
            if(sqlContext.exec("delete from RESULT where f_point = ?;", new String[] { f_point })) {
                return sqlContext.exec("delete from POINT where id = ?;", new String[] { f_point });
            }
        }

        return false;
    }

    public boolean delResult(@NotNull String f_result) {
        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<Attachment> collection = sqlContext.select("select * from ATTACHMENT where f_result = ?;", new String[] { f_result }, Attachment.class);

        if(collection != null) {
            Attachment[] array = collection.toArray(new Attachment[0]);
            if(array.length > 0) {
                String f_route = array[0].f_route;
                FileManager fileManager = new FileManager(mContext.getFilesDir());
                for (Attachment item: array) {
                    fileManager.deleteFile(f_route, item.c_name);
                }
            }
        }

        if(sqlContext.exec("delete from ATTACHMENT where f_result = ?;", new String[] { f_result })) {
            return sqlContext.exec("delete from RESULT where id = ?;", new String[] { f_result });
        }

        return false;
    }

    /**
     * получение результат для экспорта
     * @param f_route иден. маршрута
     * @return список результатов
     */
    @Nullable
    public ResultExportItem[] getResultExport(@NotNull String f_route) {
        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);

        Collection<ResultExportItem> collection = sqlContext.select("select\n" +
                "                    p.C_ADDRESS,\n" +
                "                    p.N_LATITUDE,\n" +
                "                    p.N_LONGITUDE,\n" +
                "                    p.C_DESCRIPTION,\n" +
                "                    p.B_ANOMALY,\n" +
                "                    p.N_ORDER,\n" +
                "                    p.C_IMP_ID,\n" +
                "                    p.JB_DATA,\n" +
                "                    IFNULL(rr.N_LATITUDE, 0.0) as N_RESULT_LATITUDE,\n" +
                "                    IFNULL(rr.N_LONGITUDE, 0.0) as N_RESULT_LONGITUDE,\n" +
                "                    IFNULL(rr.N_DISTANCE, -1) as N_RESULT_DISTANCE,\n" +
                "                    rr.id as F_RESULT,\n" +
                "                    rr.JB_DATA as JB_RESULT_DATA,\n" +
                "                    rr.D_DATE,\n" +
                "                    rr.C_TEMPLATE,\n" +
                "                    t.C_NAME as C_TEMPLATE_NAME,\n" +
                "                    IFNULL(t.N_ORDER, -1) as N_TEMPLATE_ORDER,\n" +
                "                    (select count(*) from ATTACHMENT as a where a.f_result = rr.id) as N_IMAGE_COUNT\n" +
                "                from POINT as p\n" +
                "                left join RESULT as rr on p.id = rr.f_point\n" +
                "                left join TEMPLATE as t on t.C_TEMPLATE = rr.C_TEMPLATE and t.f_route = ?\n" +
                "                where p.f_route = ?\n" +
                "                order by p.n_order, rr.n_date", new String[] { f_route, f_route }, ResultExportItem.class);
        if(collection != null) {
            return collection.toArray(new ResultExportItem[0]);
        }

        return null;
    }

    /**
     * Добавление результата
     * @param item результат
     * @return true - результат вставки
     */
    public boolean addResult(@NotNull Result item) {
        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        return sqlContext.insertMany(new Result[] { item });
    }

    /**
     * Получение шаблона для результата
     * @param f_route иден. маршрута
     * @param c_template имя шаблона
     * @return шаблон
     */
    @Nullable
    public Template getTemplate(@NotNull String f_route, @NotNull String c_template) {
        WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
        Collection<Template> resultCollection = sqlContext.select("SELECT * from Template as t where t.f_route = ? and t.c_template = ?", new String[] { f_route, c_template }, Template.class);

        if(resultCollection != null && resultCollection.size() > 0) {
            return resultCollection.toArray(new Template[0])[0];
        }

        return null;
    }

    /**
     * Обновление точки
     * @param f_point иден. точки
     * @param isCheck подтверждение
     * @param comment комментарий
     * @return true - результат обновления
     */
    public boolean updatePoint(@Nullable String f_point, boolean isCheck, String comment) {
        if(f_point == null) {
            return false;
        }
        Point point = getPoint(f_point);
        if(point != null) {
            point.b_check = isCheck;
            point.c_comment = comment;

            WalkerSQLContext sqlContext = WalkerApplication.getWalkerSQLContext(mContext);
            return sqlContext.insertMany(new Point[] { point });
        }

        return true;
    }
}
