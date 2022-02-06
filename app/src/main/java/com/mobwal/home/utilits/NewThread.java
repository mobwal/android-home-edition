package com.mobwal.home.utilits;

import android.app.Activity;

/**
 * Создание нового потока. Замена AsyncTask
 */
public abstract class NewThread implements Runnable {
    private final Activity mActivity;

    /**
     *
     * @param activity передаем активити, если результат нужно выполнить в главном потоке
     */
    public NewThread(Activity activity) { mActivity = activity; }

    private Thread mThread;

    @Override
    public void run() {
        mThread = new Thread(() -> {
            onBackgroundExecute();

            if(!Thread.currentThread().isInterrupted()) {
                if(mActivity == null) {
                    onPostExecute();
                } else {
                    mActivity.runOnUiThread(this::onPostExecute);
                }
            }
        });
        mThread.start();
    }

    /**
     * Выполнение в фоне
     */
    public abstract void onBackgroundExecute();

    /**
     * Выполняется после основной процедуры
     */
    public abstract void onPostExecute();

    /**
     * Запущен ли поток
     * @return true - запущен
     */
    public boolean isThreading() {
        return mThread != null;
    }

    /**
     * Прерывание
     */
    public void destroy() {
        if(mThread != null) {
            mThread.interrupt();
        }
    }
}
