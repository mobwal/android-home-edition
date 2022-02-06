package com.mobwal.home.models.db.complex;

import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * результат работ для импорта данных
 */
public class ResultExportItem {
    /**
     адрес точки
     */
    @Nullable
    public String c_address;

    /**
     широта
     */
    public Double n_latitude = 0.0;

    /**
     долгота
     */
    public Double n_longitude = 0.0;

    /**
     Описание
     */
    @Nullable
    public String c_description;

    /**
     является аномалией
     */
    public boolean b_anomaly = false;

    /**
     Поле для сортировки
     */
    public int n_order = 0;

    /**
     идентификатор для импорта из другой системы
     */
    @Nullable
    public String c_imp_id;

    @Nullable
    public String jb_data;

    @Nullable
    public String f_result;

    /**
     широта результата
     */
    public Double n_result_latitude = 0.0;

    /**
     долгота результата
     */
    public Double n_result_longitude = 0.0;

    /**
     дистанция результата
     */
    public int n_result_distance = -1;

    @Nullable
    public String jb_result_data;

    /**
     Дата выполнения
     */
    public Date d_date = null;

    /**
     Шаблон выполнения
     */
    @Nullable
    public String c_template;

    /**
     Пользовательское имя шаблона выполнения
     */
    public String c_template_name;

    /**
     сортировка шаблона
     */
    public int n_template_order = 0;

    /**
     количество изображений
     */
    public int n_image_count = 0;

    public String getId() {
        if (n_template_order > 0) {
            return n_order + "-" + n_template_order;
        } else {
            return "";
        }
    }
}
