package com.mobwal.home.ui;

/**
 * Обработчик возможности удаления строки в RecycleView
 */
public interface RecycleViewItemRemovable {
    /**
     * Разрешено удаление или нет
     * @return true - разрешено удаление
     */
    boolean isRemovable();
}
