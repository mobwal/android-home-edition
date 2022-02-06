package com.mobwal.home.ui.route;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.mobwal.home.R;

/**
 * Компонент для реализации удаления строку маршрута
 */
abstract public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final Paint mClearPaint;
    private final ColorDrawable mBackground;
    private final int backgroundColor;

    public SwipeToDeleteCallback(Context context, int dragDirs, int swipeDirs) {
        super(dragDirs, swipeDirs);

        mBackground = new ColorDrawable();
        backgroundColor = context.getResources().getColor(R.color.colorSecondary);
        mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    /**
     * Нужно для отрисовки фона и иконки
     */
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;

        boolean isCancelled = dX == 0 && !isCurrentlyActive;

        if (isCancelled) {
            clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        mBackground.setColor(backgroundColor);
        mBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        mBackground.draw(c);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(itemView.getContext().getResources().getDimensionPixelSize(R.dimen.font_16));
        paint.setTextAlign(Paint.Align.CENTER);
        String inbox = itemView.getContext().getResources().getString(R.string.remove);
        int yPos = (int) ((itemView.getTop() + itemView.getHeight() / 2)  - ((paint.descent() + paint.ascent()) / 2));
        c.drawText(inbox.toUpperCase(), itemView.getRight() - 200, yPos, paint);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, mClearPaint);
    }
}
