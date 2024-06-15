package com.example.budgettracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class backgroundcolor extends RecyclerView.ItemDecoration {
    //根据positon的奇偶画背景
    private Drawable oddBackground;
    private Drawable evenBackground;

    public backgroundcolor(Context context) {
        oddBackground = new ColorDrawable(ContextCompat.getColor(context, R.color.odd_background_color));
        evenBackground = new ColorDrawable(ContextCompat.getColor(context, R.color.even_background_color));
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int itemCount = parent.getAdapter().getItemCount();
        for (int i = 0; i < itemCount; i++) {
            View child = parent.getChildAt(i);
            if (child != null) {
                Drawable background = (i % 2 == 0) ? evenBackground : oddBackground;
                background.setBounds(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                background.draw(c);
            }
        }
    }
}
