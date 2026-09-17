package rkr.simplekeyboard.inputmethod.clipboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Minimal, lightweight clipboard / notes hub view.
 * Inflated only when the user opens the clipboard feature (lazy).
 *
 * This is a functional skeleton — replace the simple LinearLayout
 * with a proper RecyclerView for production if the history grows large.
 */
public class ClipboardView extends FrameLayout {

    public interface Listener {
        void onInsertRequested(ClipboardItem item);
        void onClose();
    }

    private Listener mListener;
    private ClipboardRepository mRepo;
    private LinearLayout mContainer;
    private EditText mSearch;
    private TextView mStatus;

    public ClipboardView(Context context) {
        super(context);
        init(context);
    }

    public ClipboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mRepo = ClipboardRepository.getInstance(context);

        // Programmatic lightweight UI (no extra layout XML dependency for this skeleton)
        setBackgroundColor(0xFF1E1E1E);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 16, 16, 16);

        // Header
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        TextView title = new TextView(context);
        title.setText("📋 Clipboard");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(18);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        header.addView(title);

        Button closeBtn = new Button(context);
        closeBtn.setText("✕");
        closeBtn.setOnClickListener(v -> {
            if (mListener != null) mListener.onClose();
        });
        header.addView(closeBtn);
        root.addView(header);

        // Search
        mSearch = new EditText(context);
        mSearch.setHint("Search…");
        mSearch.setTextColor(0xFFFFFFFF);
        mSearch.setHintTextColor(0xFFAAAAAA);
        mSearch.setSingleLine(true);
        mSearch.setOnEditorActionListener((v, actionId, event) -> {
            refresh();
            return true;
        });
        root.addView(mSearch);

        mStatus = new TextView(context);
        mStatus.setTextColor(0xFF888888);
        mStatus.setTextSize(12);
        root.addView(mStatus);

        ScrollView scroll = new ScrollView(context);
        mContainer = new LinearLayout(context);
        mContainer.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(mContainer);
        root.addView(scroll, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));

        addView(root);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void refresh() {
        mContainer.removeAllViews();
        String q = mSearch.getText() != null ? mSearch.getText().toString().trim() : "";

        List<ClipboardItem> pinned = mRepo.getPinned();
        List<ClipboardItem> recent;
        if (q.isEmpty()) {
            recent = mRepo.getRecent(50, false);
        } else {
            recent = mRepo.search(q, 50);
        }
        List<ClipboardGroup> groups = mRepo.getAllGroups();

        mStatus.setText(pinned.size() + " pinned · " + recent.size() + " shown · " + groups.size() + " groups");

        if (!pinned.isEmpty()) {
            addSectionHeader("📌 PINNED");
            for (ClipboardItem item : pinned) {
                addItemView(item);
            }
        }

        addSectionHeader("🕒 RECENT");
        if (recent.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No items yet. Copy something!");
            empty.setTextColor(0xFF888888);
            empty.setPadding(0, 8, 0, 8);
            mContainer.addView(empty);
        } else {
            for (ClipboardItem item : recent) {
                if (item.pinned) continue; // already shown
                addItemView(item);
            }
        }

        if (!groups.isEmpty()) {
            addSectionHeader("📁 GROUPS");
            for (ClipboardGroup g : groups) {
                addGroupView(g);
            }
        }
    }

    private void addSectionHeader(String title) {
        TextView h = new TextView(getContext());
        h.setText(title);
        h.setTextColor(0xFF4CAF50);
        h.setTextSize(14);
        h.setPadding(0, 16, 0, 8);
        mContainer.addView(h);
    }

    private void addItemView(final ClipboardItem item) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(12, 10, 12, 10);
        row.setBackgroundColor(0xFF2A2A2A);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 4, 0, 4);
        row.setLayoutParams(lp);

        TextView preview = new TextView(getContext());
        preview.setText(item.getPreview(120));
        preview.setTextColor(0xFFFFFFFF);
        preview.setTextSize(14);
        preview.setMaxLines(3);
        row.addView(preview);

        TextView meta = new TextView(getContext());
        meta.setText(item.length() + " chars" + (item.isNote ? " · note" : "") + (item.pinned ? " · pinned" : ""));
        meta.setTextColor(0xFF888888);
        meta.setTextSize(11);
        row.addView(meta);

        row.setOnClickListener(v -> {
            if (mListener != null) mListener.onInsertRequested(item);
        });

        row.setOnLongClickListener(v -> {
            // Simple actions for skeleton
            showItemActions(item);
            return true;
        });

        mContainer.addView(row);
    }

    private void addGroupView(final ClipboardGroup group) {
        TextView g = new TextView(getContext());
        g.setText("📁 " + group.name);
        g.setTextColor(0xFFBBDEFB);
        g.setTextSize(15);
        g.setPadding(12, 12, 12, 12);
        g.setBackgroundColor(0xFF263238);
        g.setOnClickListener(v -> {
            // In full version: expand / show items of this group
            List<ClipboardItem> items = mRepo.getByGroup(group.id);
            Toast.makeText(getContext(), group.name + ": " + items.size() + " items", Toast.LENGTH_SHORT).show();
        });
        mContainer.addView(g);
    }

    private void showItemActions(final ClipboardItem item) {
        // Minimal action sheet via Toast + sequential for skeleton
        // Full version would use a PopupMenu or bottom sheet
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(getContext());
        b.setTitle(item.getPreview(40));
        String[] actions = {"Insert", "Pin/Unpin", "Delete", "Cancel"};
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
        b.show();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refresh();
    }
}
