/*
 * Copyright (C) 2026 LightClipKeyboard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rkr.simplekeyboard.inputmethod.clipboard;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.keyboard.PanelBottomBar;
import rkr.simplekeyboard.inputmethod.keyboard.PanelTheme;

/**
 * Gboard-style clipboard panel: filter chips, a two-column card grid and the shared
 * ABC / backspace bottom bar. Colors follow the active keyboard theme.
 */
public class ClipboardView extends FrameLayout {

    public interface Listener {
        void onInsertRequested(ClipboardItem item);
        void onBackspace();
        void onClose();
    }

    private static final int FILTER_ALL = -1;
    private static final int FILTER_PINNED = -2;
    private static final int FILTER_NOTES = -3;

    private Listener mListener;
    private ClipboardRepository mRepo;
    private PanelTheme mTheme;
    private LinearLayout mContainer;
    private LinearLayout mChips;
    private ScrollView mScroll;
    private TextView mTitle;
    private long mFilter = FILTER_ALL; // >= 0 means a group id

    public ClipboardView(Context context) {
        super(context);
        init(context);
    }

    public ClipboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(final Context context) {
        mRepo = ClipboardRepository.getInstance(context);
        mTheme = PanelTheme.resolve(context);
        mTheme.applyPanelBackground(this);

        final LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // Header: title + filter chips.
        final LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(mTheme.dp(16), mTheme.dp(8), mTheme.dp(8), mTheme.dp(4));

        mTitle = new TextView(context);
        mTitle.setText("Clipboard");
        mTitle.setTextSize(15);
        mTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mTitle.setTextColor(mTheme.onSurface);
        header.addView(mTitle, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        final HorizontalScrollView chipScroll = new HorizontalScrollView(context);
        chipScroll.setHorizontalScrollBarEnabled(false);
        chipScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mChips = new LinearLayout(context);
        mChips.setOrientation(LinearLayout.HORIZONTAL);
        mChips.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        chipScroll.addView(mChips);
        final LinearLayout.LayoutParams csp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        csp.setMargins(mTheme.dp(12), 0, 0, 0);
        header.addView(chipScroll, csp);
        root.addView(header);

        // Card grid.
        mScroll = new ScrollView(context);
        mScroll.setVerticalScrollBarEnabled(false);
        mScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mContainer = new LinearLayout(context);
        mContainer.setOrientation(LinearLayout.VERTICAL);
        mContainer.setPadding(mTheme.dp(12), mTheme.dp(4), mTheme.dp(12), mTheme.dp(8));
        mScroll.addView(mContainer);
        root.addView(mScroll, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));

        // Bottom bar.
        final PanelBottomBar bar = new PanelBottomBar(context, mTheme, new PanelBottomBar.Listener() {
            @Override public void onBackToKeyboard() { if (mListener != null) mListener.onClose(); }
            @Override public void onBackspace() { if (mListener != null) mListener.onBackspace(); }
        });
        final TextView hint = new TextView(context);
        hint.setText("Tap to paste · hold for options");
        hint.setTextSize(12);
        hint.setTextColor(mTheme.onSurfaceVariant);
        hint.setGravity(Gravity.CENTER);
        hint.setSingleLine(true);
        hint.setEllipsize(TextUtils.TruncateAt.END);
        bar.getCenterSlot().addView(hint, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        root.addView(bar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        addView(root);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    // ---------------------------------------------------------------- chips

    private void rebuildChips(final List<ClipboardGroup> groups) {
        mChips.removeAllViews();
        addChip("All", FILTER_ALL);
        addChip("Pinned", FILTER_PINNED);
        addChip("Notes", FILTER_NOTES);
        for (final ClipboardGroup g : groups) {
            addChip(g.name, g.id);
        }
    }

    private void addChip(final String label, final long filter) {
        final boolean selected = filter == mFilter;
        final TextView chip = new TextView(getContext());
        chip.setText(label);
        chip.setTextSize(12);
        chip.setSingleLine(true);
        chip.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        chip.setTextColor(selected ? mTheme.onAccent : mTheme.onSurface);
        chip.setPadding(mTheme.dp(12), mTheme.dp(6), mTheme.dp(12), mTheme.dp(6));
        chip.setBackground(selected
                ? mTheme.roundRect(mTheme.accent, 16)
                : mTheme.pressable(mTheme.functionalSurface, mTheme.surfacePressed, 16));
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(mTheme.dp(3), 0, mTheme.dp(3), 0);
        chip.setLayoutParams(lp);
        chip.setOnClickListener(v -> {
            mFilter = filter;
            refresh();
        });
        mChips.addView(chip);
    }

    // ---------------------------------------------------------------- content

    public void refresh() {
        final List<ClipboardGroup> groups = mRepo.getAllGroups();
        rebuildChips(groups);
        mContainer.removeAllViews();
        mScroll.scrollTo(0, 0);

        final List<ClipboardItem> items = new ArrayList<>();
        String emptyText = "Nothing copied yet. Text you copy will show up here.";
        if (mFilter == FILTER_ALL) {
            items.addAll(mRepo.getPinned());
            for (final ClipboardItem it : mRepo.getRecent(60, false)) {
                if (!it.pinned) items.add(it);
            }
        } else if (mFilter == FILTER_PINNED) {
            items.addAll(mRepo.getPinned());
            emptyText = "Hold an item and choose Pin to keep it here.";
        } else if (mFilter == FILTER_NOTES) {
            items.addAll(mRepo.getNotes());
            emptyText = "No notes yet.";
        } else {
            items.addAll(mRepo.getByGroup(mFilter));
            emptyText = "This group is empty.";
        }

        if (items.isEmpty()) {
            final TextView empty = new TextView(getContext());
            empty.setText(emptyText);
            empty.setTextColor(mTheme.onSurfaceVariant);
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(mTheme.dp(24), mTheme.dp(40), mTheme.dp(24), mTheme.dp(40));
            mContainer.addView(empty);
            return;
        }

        // Two-column card grid.
        LinearLayout row = null;
        for (int i = 0; i < items.size(); i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                mContainer.addView(row, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            row.addView(buildCard(items.get(i)));
        }
        if (items.size() % 2 == 1 && row != null) {
            final View filler = new View(getContext());
            row.addView(filler, cardParams());
        }
    }

    private LinearLayout.LayoutParams cardParams() {
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.setMargins(mTheme.dp(4), mTheme.dp(4), mTheme.dp(4), mTheme.dp(4));
        return lp;
    }

    private View buildCard(final ClipboardItem item) {
        final LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(mTheme.dp(12), mTheme.dp(10), mTheme.dp(10), mTheme.dp(8));
        card.setBackground(mTheme.pressable(mTheme.surface, mTheme.surfacePressed, 14));
        card.setMinimumHeight(mTheme.dp(72));
        card.setLayoutParams(cardParams());

        final TextView preview = new TextView(getContext());
        preview.setText(item.getPreview(140));
        preview.setTextColor(mTheme.onSurface);
        preview.setTextSize(14);
        preview.setMaxLines(3);
        preview.setEllipsize(TextUtils.TruncateAt.END);
        card.addView(preview, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        final LinearLayout footer = new LinearLayout(getContext());
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        final TextView meta = new TextView(getContext());
        meta.setText(item.isNote ? "Note" : relativeTime(item.timestamp));
        meta.setTextColor(mTheme.onSurfaceVariant);
        meta.setTextSize(11);
        footer.addView(meta, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        final ImageView pin = new ImageView(getContext());
        pin.setImageResource(item.pinned ? R.drawable.ic_pin : R.drawable.ic_pin_outline);
        pin.setColorFilter(item.pinned ? mTheme.accent : mTheme.onSurfaceVariant, PorterDuff.Mode.SRC_IN);
        pin.setScaleType(ImageView.ScaleType.CENTER);
        pin.setBackground(mTheme.ripple(14));
        pin.setContentDescription(item.pinned ? "Unpin" : "Pin");
        pin.setOnClickListener(v -> {
            mRepo.setPinned(item.id, !item.pinned);
            refresh();
        });
        footer.addView(pin, new LinearLayout.LayoutParams(mTheme.dp(28), mTheme.dp(28)));
        card.addView(footer);

        card.setOnClickListener(v -> {
            if (mListener != null) mListener.onInsertRequested(item);
        });
        card.setOnLongClickListener(v -> {
            showItemActions(item);
            return true;
        });
        return card;
    }

    private static String relativeTime(final long ts) {
        final long diff = Math.max(0, System.currentTimeMillis() - ts);
        final long m = diff / 60000L;
        if (m < 1) return "Just now";
        if (m < 60) return m + " min ago";
        final long h = m / 60;
        if (h < 24) return h + " h ago";
        final long d = h / 24;
        return d == 1 ? "Yesterday" : d + " days ago";
    }

    private void showItemActions(final ClipboardItem item) {
        final AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setTitle(item.getPreview(40));
        final String[] actions = { "Paste", item.pinned ? "Unpin" : "Pin", "Delete" };
        b.setItems(actions, (d, which) -> {
            switch (which) {
                case 0:
                    if (mListener != null) mListener.onInsertRequested(item);
                    break;
                case 1:
                    mRepo.setPinned(item.id, !item.pinned);
                    refresh();
                    break;
                case 2:
                    mRepo.delete(item.id);
                    refresh();
                    break;
            }
        });
        final AlertDialog dialog = b.create();
        // Dialogs opened from an IME must be attached to the keyboard window.
        final Window w = dialog.getWindow();
        final IBinder token = getWindowToken();
        if (w != null && token != null) {
            final WindowManager.LayoutParams lp = w.getAttributes();
            lp.token = token;
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
            w.setAttributes(lp);
            w.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
        dialog.show();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refresh();
    }
}
