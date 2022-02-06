package com.mobwal.home.ui;

/**
 * Обработчик событий для маршрута
 */
public interface RecycleViewItemListeners {
    /**
     * Отображение информации по маршруту
     * @param id идентификатор маршрута
     */
    default void onViewItemInfo(String id) {}

    /**
     * Нажатие на элемент списка
     * @param id идентификатор маршрута
     */
    void onViewItemClick(String id);
}
