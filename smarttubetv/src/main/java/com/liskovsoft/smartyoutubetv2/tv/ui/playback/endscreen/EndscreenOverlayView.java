package com.liskovsoft.smartyoutubetv2.tv.ui.playback.endscreen;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal endscreen overlay for Android TV:
 * - Shows a 2-column grid of items.
 * - Items are focusable and clickable.
 * - Overlay can be shown/hidden by the host (PlaybackFragment).
 */
public class EndscreenOverlayView extends FrameLayout {
    public interface Listener {
        void onEndscreenItemClicked(@NonNull EndscreenItem item);
    }

    /**
     * Replace this with your own model later if you want.
     */
    public static class EndscreenItem {
        public final long startMs;
        public final long endMs;
        public final String title;
        public final String videoId; // for MVP; add playlist/channel later

        public EndscreenItem(long startMs, long endMs, String title, String videoId) {
            this.startMs = startMs;
            this.endMs = endMs;
            this.title = title;
            this.videoId = videoId;
        }
    }

    private final RecyclerView mRecycler;
    private final EndscreenAdapter mAdapter = new EndscreenAdapter();
    private Listener mListener;

    public EndscreenOverlayView(Context context) {
        this(context, null);
    }

    public EndscreenOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EndscreenOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setVisibility(GONE);
        setClickable(false);
        setFocusable(false);

        // Bottom scrim-like background (simple translucent)
        setBackgroundColor(0x55000000);

        mRecycler = new RecyclerView(context);
        mRecycler.setOverScrollMode(OVER_SCROLL_NEVER);
        mRecycler.setClipToPadding(false);
        mRecycler.setPadding(dp(24), dp(24), dp(24), dp(24));

        GridLayoutManager glm = new GridLayoutManager(context, 2);
        mRecycler.setLayoutManager(glm);
        mRecycler.setAdapter(mAdapter);

        LayoutParams lp = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        lp.gravity = Gravity.CENTER;
        addView(mRecycler, lp);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setItems(List<EndscreenItem> items) {
        mAdapter.setItems(items);
    }

    /**
     * Host calls this based on playback time.
     */
    public void show() {
        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
            // Move focus into the grid
            post(() -> {
                View first = mRecycler.getLayoutManager() != null
                        ? mRecycler.getLayoutManager().findViewByPosition(0)
                        : null;
                if (first != null) first.requestFocus();
            });
        }
    }

    public void hide() {
        if (getVisibility() != GONE) {
            setVisibility(GONE);
        }
    }

    public boolean isShowing() {
        return getVisibility() == VISIBLE;
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private GradientDrawable focusBg(boolean focused) {
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(dp(14));
        d.setStroke(dp(2), focused ? 0xFFFFFFFF : 0x33FFFFFF);
        d.setColor(focused ? 0x33FFFFFF : 0x22000000);
        return d;
    }

    private class EndscreenAdapter extends RecyclerView.Adapter<EndscreenVH> {
        private final List<EndscreenItem> mItems = new ArrayList<>();

        void setItems(List<EndscreenItem> items) {
            mItems.clear();
            if (items != null) mItems.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public EndscreenVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tv.setPadding(dp(16), dp(16), dp(16), dp(16));
            tv.setMaxLines(2);
            tv.setFocusable(true);
            tv.setFocusableInTouchMode(true);
            tv.setClickable(true);
            tv.setBackground(focusBg(false));
            tv.setOnFocusChangeListener((v, hasFocus) -> v.setBackground(focusBg(hasFocus)));

            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(96)
            );
            lp.setMargins(dp(10), dp(10), dp(10), dp(10));
            tv.setLayoutParams(lp);

            return new EndscreenVH(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull EndscreenVH holder, int position) {
            EndscreenItem item = mItems.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

    private class EndscreenVH extends RecyclerView.ViewHolder {
        private EndscreenItem mItem;

        EndscreenVH(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(v -> {
                if (mListener != null && mItem != null) {
                    mListener.onEndscreenItemClicked(mItem);
                }
            });
        }

        void bind(EndscreenItem item) {
            mItem = item;
            ((TextView) itemView).setText(item.title != null ? item.title : "(no title)");
        }
    }
}
