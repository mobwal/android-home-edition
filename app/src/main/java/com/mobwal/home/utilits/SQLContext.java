package com.mobwal.home.utilits;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import com.mobwal.home.Names;
import com.mobwal.home.WalkerApplication;

public abstract class SQLContext extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "walker.db";

    // путь к БД
    private final File databasePath;

    public SQLContext(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        databasePath = context.getDatabasePath(DATABASE_NAME);
    }

    /**
     * Создание базы данных
     * @param db подключение к БД
     */
    @Override
    public abstract void onCreate(SQLiteDatabase db);

    /**
     * Обновление базы данных
     * @param db подключение к БД
     * @param oldVersion старая версия
     * @param newVersion новая версия
     */
    @Override
    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    @Nullable
    public <T> Collection<T> select(String query, String[] args, Class<?> itemClass) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, args);

        Collection<T> results = new ArrayList<>();

        Field[] fields = itemClass.getDeclaredFields();
        if(cursor.moveToFirst()) {
            do {
                T item;
                try {
                    item = (T) itemClass.newInstance();
                } catch (IllegalAccessException | InstantiationException e) {
                    Log.d(Names.LOG_ERROR, e.toString());
                    cursor.close();
                    return null;
                }
                try {
                    for (Field field : fields) {
                        int idx = cursor.getColumnIndex(field.getName().toUpperCase());
                        if (idx == -1) {
                            continue;
                        }

                        Field declaredField = item.getClass().getDeclaredField(field.getName());
                        if (cursor.isNull(idx)) {
                            declaredField.set(item, null);
                            continue;
                        }

                        String type = field.getType().getSimpleName().toLowerCase();

                        switch (type) {
                            case "double":
                                field.set(item, cursor.getDouble(idx));
                                break;

                            case "boolean":
                                field.set(item, cursor.getInt(idx) == 1);
                                break;

                            case "long":
                                field.set(item, cursor.getLong(idx));
                                break;

                            case "int":
                            case "integer":
                                field.set(item, cursor.getInt(idx));
                                break;

                            case "date":
                                field.set(item, DateUtil.convertStringToSystemDate(cursor.getString(idx)));
                                break;

                            default:
                                field.set(item, cursor.getString(idx));
                                break;
                        }
                    }
                } catch (Exception e) {
                    WalkerApplication.Log("Ошибка выполнения запроса " + query, e);
                    continue;
                }
                results.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return results;
    }

    /**
     * Добавление записей в таблицу
     * @param array массив данных для добавления
     * @param <T> тип передаваемого массива
     */
    public <T> boolean insertMany(T[] array) {
        boolean result = false;

        if(array.length > 0) {
            T entity = array[0];
            if(exists(entity)) {
                SQLiteDatabase db = getWritableDatabase();

                try {
                    db.beginTransaction();

                    SQLStatementInsert sqlStatementInsert = new SQLStatementInsert(entity, db);

                    for (T item: array) {
                        sqlStatementInsert.bind(item);
                    }

                    db.setTransactionSuccessful();
                    result = true;
                } catch (Exception e) {
                    Log.d(Names.LOG_ERROR, e.toString());
                } finally {
                    db.endTransaction();
                    db.close();
                }
            }
        }

        return result;
    }

    /**
     * Проверка на доступность таблицы в базе данных
     * @param entity сущность
     * @return SQL - запрос
     */
    public <T> String isExistsQuery(T entity) {
        return "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + getTableName(entity) + "';";
    }

    /**
     * Получение количества строк по запросу
     * @param query SQL - запрос
     * @return Количество полученных данных
     */
    @Nullable
    public Long count(String query) {
        return count(query, null);
    }

    /**
     * Получение количества строк по запросу
     * @param query SQL - запрос
     * @return Количество полученных данных
     */
    @Nullable
    public Long count(String query, String[] selectionArgs) {
        try (SQLiteDatabase db = getReadableDatabase(); Cursor cursor = db.rawQuery(query, selectionArgs)) {
            cursor.moveToFirst();

            return cursor.getLong(0);
        } catch (Exception e) {
            WalkerApplication.Log("Ошибка вычисления количества в запросе " + query, e);
            return null;
        }
    }

    /**
     * Выполнение запроса
     * @param query SQL - запрос
     */
    public boolean exec(String query, @Nullable String[] args) {
        try (SQLiteDatabase db = getReadableDatabase()) {
            db.execSQL(query, args);
            return true;
        } catch (SQLException e) {
            WalkerApplication.Log("SQL. Ошибка выполнения запроса " + query, e);
            return false;
        }
    }

    /**
     * Проверка на доступность таблицы в базе данных
     * @param entity сущность
     * @param <T> тип сущности
     * @return результат доступности
     */
    public <T> boolean exists(T entity) {
        Long count = count(isExistsQuery(entity));
        return count != null && count > 0;
    }

    /**
     * Создание SQL - запроса на "создание" таблицы
     * @param entity сущность
     * @param pKeyName имя поля первичного ключа
     * @return SQL - запрос
     */
    public <T> String getCreateQuery(T entity, String pKeyName) {
        // "CREATE TABLE IF NOT EXISTS FIELDS " +
        // "(ROW INTEGER PRIMARY KEY, FIELD_DATA TEXT);

        String tableName = getTableName(entity);
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        Field[] fields = entity.getClass().getDeclaredFields();
        int count = fields.length;

        for (Field field: fields) {
            count--;
            String sqlTypeName = getTypeNameFromProperty(field);
            String fieldName = field.getName();

            sql.append(fieldName.toUpperCase());
            sql.append(" ");
            sql.append(sqlTypeName);
            sql.append(fieldName.equals(pKeyName) ? " PRIMARY KEY" : "");
            sql.append(count == 0 ? ");" : ", ");
        }

        return sql.toString();
    }

    /**
     * удаление базы данных
     * В рабочем коде не должно использоваться, так как приведет к удалению базы данных
     */
    @Deprecated
    public void trash() {
        close();

        if(databasePath.exists()) {
            if(!databasePath.delete()) {
                WalkerApplication.Log("Ошибка удаления базы данных " + DATABASE_NAME);
            }
        }

        File databaseLog = new File(databasePath.getParentFile(), DATABASE_NAME + "-journal");
        if(databaseLog.exists()) {
            if(!databaseLog.delete()) {
                WalkerApplication.Log("Ошибка удаления лога базы данных " + DATABASE_NAME);
            }
        }
    }

    /**
     * Получение имени класса
     * @param entity сощность
     * @return наименование класса
     */
    protected <T> String getTableName(T entity) {
        return entity.getClass().getSimpleName();
    }

    /**
     * получение SQL типа по типу поля в классе
     * @param field поле в классе
     * @return тип SQL
     */
    protected String getTypeNameFromProperty(Field field) {
        String typeName = field.getType().getSimpleName().toLowerCase();

        switch (typeName) {
            case "boolean":
            case "int":
            case "integer":
                return "INTEGER";

            case "long":
                return "INTEGER64";

            case "double":
                return "REAL";

            default:
                return "TEXT";
        }
    }
}
