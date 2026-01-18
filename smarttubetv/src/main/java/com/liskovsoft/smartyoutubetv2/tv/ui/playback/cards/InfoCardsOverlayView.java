package com.liskovsoft.smartyoutubetv2.tv.ui.playback.cards;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal "i" info cards overlay:
 * - top-right indicator (focusable)
 * - expandable list panel
 * - host calls updateForPositionMs(positionMs)
 */
public class InfoCardsOverlayView extends FrameLayout {
    public interface Listener {
        void onInfoCardClicked(@NonNull InfoCardItem item);
    }

    /**
     * Replace with your own model later if you want.
     */
    public static class InfoCardItem {
        public final long startMs;
        public final long endMs; // you can set a default window
        public final String teaserText;
        public final String videoId;

        public InfoCardItem(long startMs, long endMs, String teaserText, String videoId) {
            this.startMs = startMs;
            this.endMs = endMs;
            this.teaserText = teaserText;
            this.videoId = videoId;
        }
    }

    private final TextView mIndicator;
    private final FrameLayout mPanel;
    private final RecyclerView mRecycler;
    private final InfoCardPresenter mPresenter = new InfoCardPresenter();
    private final CardsAdapter mAdapter = new CardsAdapter();

    private Listener mListener;
    private final List<InfoCardItem> mItems = new ArrayList<>();
    private boolean mIndicatorVisible = false;

    public InfoCardsOverlayView(Context context) {
        this(context, null);
    }

    public InfoCardsOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfoCardsOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setVisibility(VISIBLE); // overlay itself always present; children toggle
        setClickable(false);
        setFocusable(false);

        // "i" indicator (top-right)
        mIndicator = new TextView(context);
        mIndicator.setText("i");
        mIndicator.setTextColor(Color.WHITE);
        mIndicator.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mIndicator.setGravity(Gravity.CENTER);
        mIndicator.setFocusable(true);
        mIndicator.setFocusableInTouchMode(true);
        mIndicator.setClickable(true);
        mIndicator.setPadding(dp(12), dp(6), dp(12), dp(6));
        mIndicator.setBackground(pillBg(false));
        mIndicator.setOnFocusChangeListener((v, hasFocus) -> v.setBackground(pillBg(hasFocus)));
        mIndicator.setOnClickListener(v -> togglePanel(true));
        mIndicator.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                togglePanel(true);
                return true;
            }
            return false;
        });

        LayoutParams indLp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        indLp.gravity = Gravity.TOP | Gravity.END;
        indLp.topMargin = dp(18);
        indLp.rightMargin = dp(18);
        addView(mIndicator, indLp);

        // Panel container
        mPanel = new FrameLayout(context);
        mPanel.setVisibility(GONE);
        mPanel.setBackgroundColor(0x66000000);

        LayoutParams panelLp = new LayoutParams(
                dp(520),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        panelLp.gravity = Gravity.TOP | Gravity.END;
        panelLp.topMargin = dp(60);
        panelLp.rightMargin = dp(18);
        addView(mPanel, panelLp);

        // Recycler inside panel
        mRecycler = new RecyclerView(context);
        mRecycler.setOverScrollMode(OVER_SCROLL_NEVER);
        mRecycler.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        mRecycler.setAdapter(mAdapter);
        mRecycler.setPadding(dp(12), dp(12), dp(12), dp(12));
        mRecycler.setClipToPadding(false);

        mPanel.addView(mRecycler, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Start hidden
        setIndicatorVisible(false);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setItems(List<InfoCardItem> items) {
        mItems.clear();
        if (items != null) mItems.addAll(items);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Host should call this periodically based on player position.
     * Minimal logic: show the indicator if ANY card is in its [start,end] window.
     */
    public void updateForPositionMs(long positionMs) {
        boolean active = false;
        for (InfoCardItem i : mItems) {
            if (positionMs >= i.startMs && positionMs <= i.endMs) {
                active = true;
                break;
            }
        }
        setIndicatorVisible(active);

        // If indicator disappears, close panel too.
        if (!active) {
            togglePanel(false);
        }
    }

    public boolean isPanelShowing() {
        return mPanel.getVisibility() == VISIBLE;
    }

    public void togglePanel(boolean show) {
        if (show && mItems.isEmpty()) {
            show = false;
        }

        mPanel.setVisibility(show ? VISIBLE : GONE);

        if (show) {
            post(() -> {
                View first = mRecycler.getLayoutManager() != null
                        ? mRecycler.getLayoutManager().findViewByPosition(0)
                        : null;
                if (first != null) first.requestFocus();
            });
        }
    }

    private void setIndicatorVisible(boolean visible) {
        mIndicatorVisible = visible;
        mIndicator.setVisibility(visible ? VISIBLE : GONE);
        if (!visible) mPanel.setVisibility(GONE);
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private GradientDrawable pillBg(boolean focused) {
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(dp(999));
        d.setStroke(dp(2), focused ? 0xFFFFFFFF : 0x33FFFFFF);
        d.setColor(focused ? 0x33FFFFFF : 0x22000000);
        return d;
    }

    private class CardsAdapter extends RecyclerView.Adapter<InfoCardPresenter.VH> {
        @NonNull
        @Override
        public InfoCardPresenter.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            InfoCardPresenter.VH vh = mPresenter.onCreateViewHolder(parent);
            vh.itemView.setOnClickListener(v -> {
                int pos = mRecycler.getChildAdapterPosition(v);
                if (pos == RecyclerView.NO_POSITION) return;

                InfoCardItem item = mItems.get(pos);
                if (mListener != null) mListener.onInfoCardClicked(item);
                togglePanel(false);
            });

            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull InfoCardPresenter.VH holder, int position) {
            mPresenter.onBindViewHolder(holder, mItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }
}
