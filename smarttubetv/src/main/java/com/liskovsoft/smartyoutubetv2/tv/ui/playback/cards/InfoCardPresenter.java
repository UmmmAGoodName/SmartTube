package com.liskovsoft.smartyoutubetv2.tv.ui.playback.cards;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Minimal presenter-like helper: creates/binds a focusable row view for TV.
 */
public class InfoCardPresenter {

    public static class VH extends RecyclerView.ViewHolder {
        public VH(@NonNull View itemView) {
            super(itemView);
        }
    }

    public VH onCreateViewHolder(@NonNull ViewGroup parent) {
        TextView tv = new TextView(parent.getContext());
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tv.setPadding(dp(parent, 16), dp(parent, 14), dp(parent, 16), dp(parent, 14));
        tv.setMaxLines(2);

        tv.setFocusable(true);
        tv.setFocusableInTouchMode(true);
        tv.setClickable(true);

        tv.setBackground(bg(parent, false));
        tv.setOnFocusChangeListener((v, hasFocus) -> v.setBackground(bg(parent, hasFocus)));

        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(dp(parent, 10), dp(parent, 8), dp(parent, 10), dp(parent, 8));
        tv.setLayoutParams(lp);

        return new VH(tv);
    }

    public void onBindViewHolder(@NonNull VH holder, @NonNull InfoCardsOverlayView.InfoCardItem item) {
        ((TextView) holder.itemView).setText(item.teaserText != null ? item.teaserText : "(info card)");
    }

    private static int dp(ViewGroup parent, int v) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, parent.getResources().getDisplayMetrics());
    }

    private static GradientDrawable bg(ViewGroup parent, boolean focused) {
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(dp(parent, 14));
        d.setStroke(dp(parent, 2), focused ? 0xFFFFFFFF : 0x33FFFFFF);
        d.setColor(focused ? 0x33FFFFFF : 0x22000000);
        return d;
    }
}
